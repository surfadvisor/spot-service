package com.surf.advisor.spot.model;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.surf.advisor.spot.web.api.model.IntegerRange;
import com.surf.advisor.spot.web.api.model.SpotFilters;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.amazonaws.util.CollectionUtils.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.apache.commons.codec.digest.DigestUtils.md2Hex;

@Getter
@RequiredArgsConstructor
public enum SpotFilterColumn {

    STATUS(SpotRecord.STATUS, f -> mapSet(f.getStatus(), Enum::name), (c, f) -> c.includes(f.getStatus()), f -> values(f.getStatus())),
    COUNTRY(SpotRecord.COUNTRY, f -> mapSet(f.getCountry(), Enum::name), (c, f) -> c.includes(f.getCountry()), f -> values(f.getCountry())),
    DIFFICULTY(SpotRecord.DIFFICULTY, f -> mapSet(f.getDifficulty(), Enum::name), (c, f) -> c.includes(f.getDifficulty()), f -> values(f.getDifficulty())),
    WATER_TYPE(SpotRecord.WATER_TYPE, f -> mapSet(f.getWaterType(), Enum::name), (c, f) -> c.includes(f.getWaterType()), f -> values(f.getWaterType())),
    DEPTH(SpotRecord.DEPTH, f -> mapSet(f.getDepth(), Enum::name), (c, f) -> c.includes(f.getDepth()), f -> values(f.getDepth())),

    SPORT_TYPES(SpotRecord.SPORT_TYPES, null, (c, f) -> c.hasAny(f.getSportTypes()), f -> values(f.getSportTypes())),
    FACILITIES(SpotRecord.FACILITIES, null, (c, f) -> c.hasAll(f.getFacilities()), f -> values(f.getFacilities())),

    WAVE_AVG_PERIOD(SpotRecord.WAVE_AVG_PERIOD, null, (c, f) -> c.range(f.getWaveAvgPeriod()), f -> rangeValues(f.getWaveAvgPeriod())),
    WAVE_AVG_SIZE(SpotRecord.WAVE_AVG_SIZE, null, (c, f) -> c.range(f.getWaveAvgSize()), f -> rangeValues(f.getWaveAvgSize())),

    STATE(SpotRecord.STATE, null, (c, f) -> c.equals(f.getState()), f -> values(f.getState())),
    CITY(SpotRecord.CITY, null, (c, f) -> c.equals(f.getCity()), f -> values(f.getCity())),
    NAME(SpotRecord.NAME, null, (c, f) -> c.equals(f.getName()), f -> values(f.getName()));

    private final String name;
    private final Function<SpotFilters, Set<String>> rangeKeyValueRetriever;
    private final BiFunction<SpotFilterColumn, SpotFilters, String> filterExpressionGenerator;
    private final Function<SpotFilters, Map<String, AttributeValue>> filterExpressionValuesProvider;

    private static <S, T> Set<T> mapSet(Collection<S> source, Function<S, T> mapper) {
        return ofNullable(source).orElse(emptyList()).stream().filter(Objects::nonNull).map(mapper).collect(toSet());
    }

    private String equals(String value) {
        return ofNullable(value).filter(StringUtils::isNotBlank)
            .map(SpotFilterColumn::computeVarName)
            .map(var -> format("%s = %s", name, var)).orElse("");
    }

    private String range(IntegerRange range) {
        if (range != null) {
            return Map.of(" >= ", range.getFrom(), " <= ", range.getTo()).entrySet().stream()
                .filter(e -> nonNull(e.getValue()))
                .map(e -> format("(%s%s%s)", name, e.getKey(), computeVarName(e.getValue())))
                .collect(joining(" AND "));
        }
        return "";
    }

    private String includes(List<?> values) {
        if (!isNullOrEmpty(values)) {
            var body = values.stream().map(SpotFilterColumn::computeVarName).collect(joining(", "));
            return format("%s IN (%s)", name, body);
        }
        return "";
    }

    private String hasAll(List<?> values) {
        return has(values, " AND ");
    }

    private String hasAny(List<?> values) {
        return has(values, " OR ");
    }

    private String has(List<?> values, String delimiter) {
        if (!isNullOrEmpty(values)) {
            var body = values.stream().map(SpotFilterColumn::computeVarName)
                .map(var -> format("contains (%s, %s)", name, var)).collect(joining(delimiter));
            return format("(%s)", body);
        }
        return "";
    }

    private static Map<String, AttributeValue> values(List<?> values) {
        if (!isNullOrEmpty(values)) {
            return values.stream().map(Object::toString)
                .collect(toMap(SpotFilterColumn::computeVarName, AttributeValue::new));
        }
        return Map.of();
    }

    private static Map<String, AttributeValue> values(String o) {
        return values(isNull(o) ? null : List.of(o));
    }

    private static Map<String, AttributeValue> rangeValues(IntegerRange range) {
        if (range != null) {
            return Stream.of(range.getFrom(), range.getTo()).filter(Objects::nonNull).map(Object::toString)
                .collect(toMap(SpotFilterColumn::computeVarName, n -> new AttributeValue().withN(n)));
        }
        return Map.of();
    }

    private static String computeVarName(Object o) {
        return ":v_" + md2Hex(o.toString());
    }

}
