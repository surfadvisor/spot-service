package com.surf.advisor.spot.model;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.surf.advisor.spot.web.api.model.*;
import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.surf.advisor.spot.util.RangeKeyUtils.buildRangeKey;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class SpotRecord {

    public static final String ID = "id";
    private static final String RANGE_KEY = "rangeKey";

    static final String STATUS = "spot_status";
    static final String COUNTRY = "country";
    static final String STATE = "spot_state";
    static final String CITY = "city";
    static final String NAME = "name";

    private static final String SPORT_TYPES = "sportTypes";
    private static final String WATER_TYPE = "waterType";
    private static final String FACILITIES = "facilities";
    private static final String DIFFICULTY = "difficulty";
    private static final String DEPTH = "depth";
    private static final String WAVE_AVG_PERIOD = "waveAvgPeriod";
    private static final String WAVE_AVG_SIZE = "waveAvgSize";

    private static final String BEST_WIND_DIRECTIONS = "bestWindDirections";
    private static final String WORST_WIND_DIRECTIONS = "worstWindDirections";
    private static final String POPULARITY = "popularity";
    private static final String DANGERS = "dangers";
    private static final String CHOP_SIZE = "chopSize";
    private static final String WAVE_QUALITY = "waveQuality";
    private static final String WAVE_MAX_SIZE = "waveMaxSize";
    private static final String WATER_CURRENT = "waterCurrent";

    private static final String PHOTO_URLS = "photoUrls";
    private static final String LIVE_CAMERA = "liveCamera";
    private static final String DESCRIPTIONS = "descriptions";

    @Getter
    private Map<String, AttributeValue> values = new HashMap<>();

    public SpotRecord(Spot spot) {

        setStr(ID, spot.getId());
        setStr(STATUS, spot.getStatus());
        setStr(COUNTRY, spot.getCountry());
        setStr(STATE, spot.getState());
        setStr(CITY, spot.getCity());
        setStr(NAME, spot.getName());

        setEnumList(SPORT_TYPES, spot.getSportTypes());
        setStr(WATER_TYPE, spot.getWaterType());
        setEnumList(FACILITIES, spot.getFacilities());
        setStr(DIFFICULTY, spot.getDifficulty());
        setStr(DEPTH, spot.getDepth());
        setInt(WAVE_AVG_PERIOD, spot.getWaveAvgPeriod());
        setInt(WAVE_AVG_SIZE, spot.getWaveAvgSize());

        setEnumList(BEST_WIND_DIRECTIONS, spot.getBestWindDirections());
        setEnumList(WORST_WIND_DIRECTIONS, spot.getWorstWindDirections());
        setStr(POPULARITY, spot.getPopularity());
        setStr(DANGERS, spot.getDangers());
        setStr(CHOP_SIZE, spot.getChopSize());
        setInt(WAVE_QUALITY, spot.getWaveQuality());
        setInt(WAVE_MAX_SIZE, spot.getWaveMaxSize());
        setStr(WATER_CURRENT, spot.getWaterCurrent());

        setList(PHOTO_URLS, spot.getPhotoUrls());
        setList(LIVE_CAMERA, spot.getLiveCamera());
        setMap(DESCRIPTIONS, spot.getDescriptions(), d -> d.getLanguage().name(), Description::getContent);

        values.put(RANGE_KEY, buildRangeKey(spot));
    }

    public SpotRecord(Map<String, AttributeValue> values) {
        this.values.putAll(values);
    }

    public Spot toResponse() {
        var spot = new Spot();

        spot.setId(getStr(ID));
        spot.setStatus(getStr(STATUS, SpotStatus::valueOf));
        spot.setCountry(getStr(COUNTRY, CountryCode::valueOf));
        spot.setState(getStr(STATE));
        spot.setCity(getStr(CITY));
        spot.setName(getStr(NAME));

        spot.setSportTypes(getList(SPORT_TYPES, SportType::valueOf));
        spot.setWaterType(getStr(WATER_TYPE, WaterType::valueOf));
        spot.setFacilities(getList(FACILITIES, SpotFacility::valueOf));
        spot.setDifficulty(getStr(DIFFICULTY, Difficulty::valueOf));
        spot.setDepth(getStr(DEPTH, Depth::valueOf));
        spot.setWaveAvgPeriod(getInt(WAVE_AVG_PERIOD));
        spot.setWaveAvgSize(getInt(WAVE_AVG_SIZE));

        spot.setBestWindDirections(getList(BEST_WIND_DIRECTIONS, BasicWindDirection::valueOf));
        spot.setWorstWindDirections(getList(WORST_WIND_DIRECTIONS, BasicWindDirection::valueOf));
        spot.setPopularity(getStr(POPULARITY, Popularity::valueOf));
        spot.setDangers(getStr(DANGERS));
        spot.setChopSize(getStr(CHOP_SIZE, ChopSize::valueOf));


        spot.setPhotoUrls(getList(PHOTO_URLS));
        spot.setLiveCamera(getList(LIVE_CAMERA));
        spot.setDescriptions(getMap(DESCRIPTIONS, (k, v) ->
            new Description().language(CountryCode.fromValue(k)).content(v)));

        return spot;
    }

    private void setStr(String field, Enum<?> value) {
        ofNullable(value).map(Enum::name).ifPresent(str -> setStr(field, str));
    }

    private void setStr(String field, String value) {
        ofNullable(value).map(AttributeValue::new).ifPresent(val -> values.put(field, val));
    }

    private String getStr(String field) {
        return getStr(field, identity());
    }

    private <T> T getStr(String field, Function<String, T> mapper) {
        return of(field).map(values::get).map(AttributeValue::getS).map(mapper).orElse(null);
    }

    private void setInt(String field, Integer value) {
        ofNullable(value).map(Object::toString).map(new AttributeValue()::withN)
            .ifPresent(val -> values.put(field, val));
    }

    private Integer getInt(String field) {
        return of(field).map(values::get).map(AttributeValue::getN).map(Integer::valueOf).orElse(null);
    }

    private <E extends Enum<E>> void setEnumList(String field, List<E> input) {
        setList(field, input, Enum::name);
    }

    private void setList(String field, List<String> input) {
        setList(field, input, identity());
    }

    private <T> void setList(String field, List<T> input, Function<T, String> mapper) {
        if (input != null) {
            var attributeValues = input.stream().map(mapper).map(AttributeValue::new).collect(toList());
            values.put(field, new AttributeValue().withL(attributeValues));
        }
    }

    private List<String> getList(String field) {
        return getList(field, identity());
    }

    private <T> List<T> getList(String field, Function<String, T> mapper) {
        return ofNullable(values.get(field)).map(AttributeValue::getL).stream().flatMap(Collection::stream)
            .map(AttributeValue::getS).map(mapper).collect(toList());
    }

    private <E> void setMap(String field, List<E> entries, Function<E, String> key, Function<E, String> val) {
        if (entries != null) {
            var valueMap = entries.stream().collect(toMap(key, e -> new AttributeValue(val.apply(e))));
            values.put(field, new AttributeValue().withM(valueMap));
        }
    }

    private <E> List<E> getMap(String field, BiFunction<String, String, E> mapper) {
        return ofNullable(values.get(field)).map(AttributeValue::getM).map(Map::entrySet).stream()
            .flatMap(Collection::stream)
            .map(e -> mapper.apply(e.getKey(), e.getValue().getS())).collect(toList());
    }

}
