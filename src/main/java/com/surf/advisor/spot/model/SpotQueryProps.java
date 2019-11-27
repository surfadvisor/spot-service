package com.surf.advisor.spot.model;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.surf.advisor.spot.web.api.model.SpotFilters;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;
import java.util.stream.Stream;

import static com.surf.advisor.spot.util.RangeKeyUtils.*;
import static com.surf.advisor.spot.util.RecordUtils.exclusiveStartKey;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

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
        if(isEmpty(rangeKeyPhrases)) {
            rangeKeyPhrases = List.of("");
        }
        rangeKeyPhrases.forEach(rangeKeyPhrase ->
            new HashSet<>(filters.getIds()).stream()
                .map(id -> new KeyCondition(id, rangeKeyPhrase, valueMap))
                .forEach(keyConditions::add)
        );
    }

    private void setFilterExpression(Deque<SpotFilterColumn> leftRangeKeyColumns) {

        String filterExpression = Stream.concat(leftRangeKeyColumns.stream(), FILTERS_NON_RANGE_KEY_COLUMNS.stream())
            .map(col -> new FilterParam(col, filters))
            .filter(FilterParam::isApplicable)
            .peek(param -> valueMap.put(param.getVarName(), new AttributeValue(param.getValue())))
            .map(param -> format("%s = %s", param.getColumnName(), param.getVarName()))
            .collect(joining(" AND "));

        filterExp = isNotBlank(filterExpression) ? filterExpression : null;
    }

    @Getter
    private static class FilterParam {

        private final boolean applicable;
        private final String columnName;
        private final String varName;
        private final String value;

        FilterParam(SpotFilterColumn column, SpotFilters filters) {
            value = "";//column.getGetter().apply(filters);

            applicable = isNotBlank(value);
            columnName = column.getName();
            varName = ":v_" + columnName;
        }
    }
}
