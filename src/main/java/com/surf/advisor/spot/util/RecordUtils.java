package com.surf.advisor.spot.util;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RecordUtils {

    public static String lastEvaluatedKey(Map<String, AttributeValue> key) {
        return key != null ? key.toString() : null;
    }

    public static Map<String, AttributeValue> exclusiveStartKey(String lastEvaluatedKey) {
        return null;
    }
}
