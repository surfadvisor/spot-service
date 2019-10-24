package com.surf.advisor.spot.model;

import static com.surf.advisor.spot.util.RangeKeyUtils.RANGE_KEY_SEPARATOR;
import static com.surf.advisor.spot.util.RangeKeyUtils.normalize;
import static com.surf.advisor.spot.util.RangeKeyUtils.rangeKeyColumns;
import static com.surf.advisor.spot.util.RecordUtils.exclusiveStartKey;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.surf.advisor.spot.web.api.model.SpotFilters;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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

        var rangeKeyPhrase = new StringBuilder();
        var rangeKeyColumns = rangeKeyColumns();

        String searchValue;
        do {
            var column = rangeKeyColumns.pop();
            searchValue = column.getGetter().apply(filters);

            if (isNotBlank(searchValue)) {
                rangeKeyPhrase.append(normalize(searchValue)).append(RANGE_KEY_SEPARATOR);
            }
        } while (isNotBlank(searchValue) && !rangeKeyColumns.isEmpty());

        setFilterExpression(idsSpecified() ? rangeKeyColumns : rangeKeyColumns());
        setKeyConditionExpresion(rangeKeyPhrase.toString());
    }

    public Map<String, AttributeValue> getValueMap() {
        return isEmpty(valueMap) ? null : valueMap;
    }

    private boolean idsSpecified() {
        return !isEmpty(filters.getIds());
    }

    private void setKeyConditionExpresion(String rangeKeyPhrase) {

        if (idsSpecified()) {
            if (isNotBlank(rangeKeyPhrase)) {
                valueMap.put(":v_rangeKey", new AttributeValue(rangeKeyPhrase));
            }

            String keyExpSuffix = isNotBlank(rangeKeyPhrase) ?
                " and begins_with(rangeKey, :v_rangeKey)" : "";

            new HashSet<>(filters.getIds()).stream()
                .map(id -> new KeyCondition(id, keyExpSuffix, valueMap))
                .forEach(keyConditions::add);
        }
    }

    private void setFilterExpression(Deque<SpotFilterColumn> leftRangeKeyColumns) {

        String filterExpression = leftRangeKeyColumns.stream()
            .map(col -> new FilterParam(col, filters))
            .filter(FilterParam::isApplicable)
            .peek(param -> valueMap.put(param.getVarName(), new AttributeValue(param.getValue())))
            .map(param -> format("%s = %s", param.getColumnName(), param.getVarName()))
            .collect(joining(" AND "));

        filterExp = isNotBlank(filterExpression) ? filterExpression : null;
    }

    @Getter
    private class FilterParam {

        private final boolean applicable;
        private final String columnName;
        private final String varName;
        private final String value;

        FilterParam(SpotFilterColumn column, SpotFilters filters) {
            value = column.getGetter().apply(filters);

            applicable = isNotBlank(value);
            columnName = column.getName();
            varName = ":v_" + columnName;
        }
    }
}
