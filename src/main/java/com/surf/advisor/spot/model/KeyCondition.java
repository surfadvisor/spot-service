package com.surf.advisor.spot.model;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
public class KeyCondition {

    private String expression;
    private final Map<String, AttributeValue> valueMap;

    KeyCondition(String id, String rangeKeyPhrase, Map<String, AttributeValue> valueMap) {

        this.valueMap = new HashMap<>(valueMap);

        String idVariableName = ":v_" + id.replaceAll("[^A-z\\d]", "");
        String rangeKeyVariableName = idVariableName + "R";

        this.expression = format("id = %s ", idVariableName);
        this.valueMap.put(idVariableName, new AttributeValue(id));

        if (isNotBlank(rangeKeyPhrase)) {
            this.expression += format(" and begins_with(rangeKey, %s)", rangeKeyVariableName);
            this.valueMap.put(rangeKeyVariableName, new AttributeValue(rangeKeyPhrase));
        }
    }
}
