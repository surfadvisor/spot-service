package com.surf.advisor.spot.model;

import static java.lang.String.format;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class KeyCondition {

    private final String expression;
    private final Map<String, AttributeValue> valueMap;

    public KeyCondition(String id, String expSuffix, Map<String, AttributeValue> valueMap) {

        String variableName = ":v_" + id.replaceAll("[^A-z\\d]", "");

        this.expression = format("id = %s ", variableName) + expSuffix;
        this.valueMap = new HashMap<>(valueMap);

        this.valueMap.put(variableName, new AttributeValue(id));
    }
}
