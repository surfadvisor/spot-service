package com.surf.advisor.spot.model;

import static com.surf.advisor.spot.util.RangeKeyUtils.buildRangeKey;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.surf.advisor.spot.web.api.model.Spot;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;

public class SpotRecord {

    public static final String ID = "id";
    private static final String RANGE_KEY = "rangeKey";

    static final String COUNTRY = "country";
    static final String STATE = "spot_state";
    static final String CITY = "city";

    static final String NAME = "name";
    private static final String PHOTO_URLS = "photoUrls";
    static final String STATUS = "spot_status";

    @Getter
    private Map<String, AttributeValue> values = new HashMap<>();

    public SpotRecord(Spot spot) {

        setStr(ID, spot.getId());
        setStr(NAME, spot.getName());
        setStr(STATUS, spot.getStatus().name());
        setStr(COUNTRY, spot.getCountry());
        setStr(STATE, spot.getState());
        setStr(CITY, spot.getCity());

        ofNullable(spot.getPhotoUrls()).ifPresent(urls -> {
            List<AttributeValue> urlList = urls.stream().map(AttributeValue::new).collect(toList());

            values.put(PHOTO_URLS, new AttributeValue().withL(urlList));
        });

        values.put(RANGE_KEY, buildRangeKey(spot));
    }

    public SpotRecord(Map<String, AttributeValue> values) {
        this.values.putAll(values);
    }

    public Spot toResponse() {
        var spot = new Spot();

        spot.setId(getStr(ID));
        spot.setName(getStr(NAME));
        spot.setStatus(getStr(STATUS, Spot.StatusEnum::fromValue));
        spot.setCountry(getStr(COUNTRY));
        spot.setState(getStr(STATE));
        spot.setCity(getStr(CITY));

        ofNullable(values.get(PHOTO_URLS))
            .map(urls -> urls.getL().stream().map(AttributeValue::getS).collect(toList()))
            .ifPresent(spot::setPhotoUrls);

        return spot;
    }

    private void setStr(String field, String value) {
        values.put(field, new AttributeValue(value));
    }

    private String getStr(String field) {
        return getStr(field, identity());
    }

    private <T> T getStr(String field, Function<String, T> mapper) {
        return of(field).map(values::get).map(AttributeValue::getS).map(mapper).orElse(null);
    }

}
