package com.surf.advisor.spot.model;

import com.surf.advisor.spot.web.api.model.SpotFilters;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

@Getter
@RequiredArgsConstructor
public enum SpotFilterColumn {

    STATUS(SpotRecord.STATUS, f -> mapSet(f.getStatus(), Enum::name), null),
    COUNTRY(SpotRecord.COUNTRY, f -> mapSet(f.getCountry(), Enum::name), null),
    DIFFICULTY(SpotRecord.DIFFICULTY, f -> mapSet(f.getDifficulty(), Enum::name), null),
    WATER_TYPE(SpotRecord.WATER_TYPE, f -> mapSet(f.getWaterType(), Enum::name), null),
    DEPTH(SpotRecord.DEPTH, f -> mapSet(f.getDepth(), Enum::name), null),

    SPORT_TYPES(SpotRecord.SPORT_TYPES, null, null),
    FACILITIES(SpotRecord.FACILITIES, null, null),

    WAVE_AVG_PERIOD(SpotRecord.WAVE_AVG_PERIOD, null, null),
    WAVE_AVG_SIZE(SpotRecord.WAVE_AVG_SIZE, null, null),

    STATE(SpotRecord.STATE, null, SpotFilters::getState),
    CITY(SpotRecord.CITY, null, SpotFilters::getCity),
    NAME(SpotRecord.NAME, null, SpotFilters::getName);

    private final String name;
    private final Function<SpotFilters, Set<String>> rangeKeyValueRetriever;
    private final Function<SpotFilters, String> filterExpressionGenerator;

    private static <S, T> Set<T> mapSet(Collection<S> source, Function<S, T> mapper) {
        return ofNullable(source).orElse(emptyList()).stream().filter(Objects::nonNull).map(mapper).collect(toSet());
    }

}
