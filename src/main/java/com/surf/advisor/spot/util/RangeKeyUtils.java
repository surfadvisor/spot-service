package com.surf.advisor.spot.util;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.surf.advisor.spot.model.SpotFilterColumn;
import com.surf.advisor.spot.web.api.model.Spot;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class RangeKeyUtils {

    public static final String RANGE_KEY_SEPARATOR = "#";

    private static final List<Function<Spot, String>> SPOT_RANGE_KEY_GETTERS = List.of(
        s -> ofNullable(s.getStatus()).map(Enum::name).orElse(null),
        s -> ofNullable(s.getCountry()).map(Enum::name).orElse(null),
        Spot::getState,
        Spot::getCity,
        Spot::getName
    );

    private static final SpotFilterColumn[] FILTERS_RANGE_KEY_GETTERS = SpotFilterColumn.values();

    public static AttributeValue buildRangeKey(Spot spot) {
        String rangeKey = SPOT_RANGE_KEY_GETTERS.stream()
            .map(getter -> getter.apply(spot))
            .map(RangeKeyUtils::normalize)
            .collect(joining(RANGE_KEY_SEPARATOR));

        return new AttributeValue(rangeKey);
    }

    public static Deque<SpotFilterColumn> rangeKeyColumns() {
        return new LinkedList<>(asList(FILTERS_RANGE_KEY_GETTERS));
    }

    public static String normalize(String input) {
        return ofNullable(input)
            .map(StringUtils::stripAccents)
            .map(String::toLowerCase)
            .orElse("");
    }

}
