package com.surf.advisor.spot.util;

import com.surf.advisor.spot.model.SpotFilterColumn;
import com.surf.advisor.spot.web.api.model.Spot;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class RangeKeyUtils {

    public static final String RANGE_KEY_SEPARATOR = "#";

    private static final List<Function<Spot, Enum<?>>> SPOT_RANGE_KEY_ENUM_GETTERS = List.of(
        Spot::getStatus,
        Spot::getCountry,
        Spot::getDifficulty,
        Spot::getWaterType,
        Spot::getDepth
    );

    private static final List<Function<Spot, String>> SPOT_RANGE_KEY_GETTERS
        = SPOT_RANGE_KEY_ENUM_GETTERS.stream().map(RangeKeyUtils::stringGetter).collect(toList());

    private static Function<Spot, String> stringGetter(Function<Spot, Enum<?>> enumGetter) {
        return s -> ofNullable(s).map(enumGetter).map(Enum::name).orElse(null);
    }

    private static final SpotFilterColumn[] FILTERS_RANGE_KEY_GETTERS = SpotFilterColumn.values();

    public static String buildRangeKey(Spot spot) {
        return SPOT_RANGE_KEY_GETTERS.stream()
            .map(getter -> getter.apply(spot))
            .map(RangeKeyUtils::normalize)
            .collect(joining(RANGE_KEY_SEPARATOR));
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
