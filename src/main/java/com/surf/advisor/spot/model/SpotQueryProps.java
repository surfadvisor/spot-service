package com.surf.advisor.spot.model;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.surf.advisor.spot.web.api.model.SpotFilters;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Stream;

import static com.surf.advisor.spot.util.RangeKeyUtils.*;
import static com.surf.advisor.spot.util.RecordUtils.exclusiveStartKey;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Getter
@ToString
public class SpotQueryProps {

    private final SpotFilters filters;

    private List<KeyCondition> keyConditions = new LinkedList<>();
    private String filterExp;

    private Map<String, AttributeValue> valueMap = new HashMap<>();
    private Map<String, AttributeValue> exclusiveStartKey;

    private Integer limit;

    @Setter
    private String projectionExp;


    public SpotQueryProps(SpotFilters spotFilters, int limit, String lastEvaluatedKey) {
        this.filters = spotFilters;

        exclusiveStartKey = exclusiveStartKey(lastEvaluatedKey);
        this.limit = limit > 0 ? limit : null;

        var rangeKeyColumns = rangeKeyColumns();
        var rangeKeyPhrases = generateConditionKeyExpressions(filters, rangeKeyColumns);

        setFilterExpression(idsSpecified() ? rangeKeyColumns : rangeKeyColumns());
        setKeyConditionExpresion(rangeKeyPhrases);
    }

    public Map<String, AttributeValue> getValueMap() {
        return isEmpty(valueMap) ? null : valueMap;
    }

    private boolean idsSpecified() {
        return !isEmpty(filters.getIds());
    }

    private void setKeyConditionExpresion(List<String> rangeKeyPhrases) {
        if (idsSpecified()) {
            if (isEmpty(rangeKeyPhrases)) {
                rangeKeyPhrases = List.of("");
            }
            rangeKeyPhrases.forEach(rangeKeyPhrase ->
                new HashSet<>(filters.getIds()).stream()
                    .map(id -> new KeyCondition(id, rangeKeyPhrase, valueMap))
                    .forEach(keyConditions::add)
            );
        }
    }

    private void setFilterExpression(Deque<SpotFilterColumn> leftRangeKeyColumns) {

        String filterExpression = Stream.concat(leftRangeKeyColumns.stream(), FILTERS_NON_RANGE_KEY_COLUMNS.stream())
            .peek(col -> valueMap.putAll(col.getFilterExpressionValuesProvider().apply(filters)))
            .map(col -> col.getFilterExpressionGenerator().apply(col, filters))
            .filter(StringUtils::isNotBlank)
            .collect(joining(" AND "));

        filterExp = isNotBlank(filterExpression) ? filterExpression : null;

        log.debug("Computed filter dynamoDB expression: [{}]", filterExp);
    }
}
