package com.surf.advisor.spot.util;

import com.surf.advisor.spot.model.SpotFilterColumn;
import com.surf.advisor.spot.web.api.model.Spot;
import com.surf.advisor.spot.web.api.model.SpotFilters;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;

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

    private static final List<SpotFilterColumn> FILTERS_RANGE_KEY_COLUMNS;
    public static final List<SpotFilterColumn> FILTERS_NON_RANGE_KEY_COLUMNS;

    static {
        var filterRangeKeyColumns = Stream.of(SpotFilterColumn.values())
            .collect(partitioningBy(column -> column.getRangeKeyValueRetriever() != null));

        FILTERS_RANGE_KEY_COLUMNS = unmodifiableList(filterRangeKeyColumns.get(true));
        FILTERS_NON_RANGE_KEY_COLUMNS = unmodifiableList(filterRangeKeyColumns.get(false));
    }

    public static String buildRangeKey(Spot spot) {
        return SPOT_RANGE_KEY_GETTERS.stream()
            .map(getter -> getter.apply(spot))
            .map(RangeKeyUtils::normalize)
            .collect(joining(RANGE_KEY_SEPARATOR));
    }

    public static Deque<SpotFilterColumn> rangeKeyColumns() {
        return new LinkedList<>(FILTERS_RANGE_KEY_COLUMNS);
    }

    private static String normalize(String input) {
        return ofNullable(input)
            .map(StringUtils::stripAccents)
            .map(String::toLowerCase)
            .orElse("");
    }

    public static List<String> generateConditionKeyExpressions(SpotFilters filters, Deque<SpotFilterColumn> columns) {
        if (!columns.isEmpty()) {

            var currentColumnValues = of(columns.pop()).map(col -> col.getRangeKeyValueRetriever().apply(filters))
                .filter(values -> !values.isEmpty());

            if (currentColumnValues.isPresent()) {
                List<String> furtherValues = generateConditionKeyExpressions(filters, columns);

                return currentColumnValues.get().stream()
                    .map(RangeKeyUtils::normalize)
                    .filter(StringUtils::isNotBlank)
                    .flatMap(currVal -> {
                    if (!furtherValues.isEmpty()) {
                        return furtherValues.stream().map(furVal -> currVal + RANGE_KEY_SEPARATOR + furVal);
                    }
                    return Stream.of(currVal);
                }).collect(toList());
            }
        }
        return emptyList();
    }

}
