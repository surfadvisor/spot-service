package com.surf.advisor.spot.domain;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.surf.advisor.spot.web.api.model.Spot;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.Getter;

public class SpotRecord {

    private static final String SEPARATOR = "#";

    private static final String ID = "id";
    private static final String RANGE_KEY = "rangeKey";

    private static final String COUNTRY = "country";
    private static final String STATE = "state";
    private static final String CITY = "city";

    private static final String NAME = "name";
    private static final String PHOTO_URLS = "photoUrls";
    private static final String STATUS = "status";

    @Getter
    private Map<String, AttributeValue> values = new HashMap<>();

    public SpotRecord(Spot spot) {

        values.put(ID, new AttributeValue(spot.getId()));
        values.put(NAME, new AttributeValue(spot.getName()));
        values.put(STATUS, new AttributeValue(spot.getStatus().name()));
        values.put(COUNTRY, new AttributeValue(spot.getCountry()));
        values.put(STATE, new AttributeValue(spot.getState()));
        values.put(CITY, new AttributeValue(spot.getCity()));

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

        spot.setId(values.get(ID).getS());
        spot.setName(values.get(NAME).getS());
        spot.setStatus(Spot.StatusEnum.fromValue(values.get(STATUS).getS()));
        spot.setCountry(values.get(COUNTRY).getS());
        spot.setState(values.get(STATE).getS());
        spot.setCity(values.get(CITY).getS());

        ofNullable(values.get(PHOTO_URLS))
            .map(urls -> urls.getL().stream().map(AttributeValue::getS).collect(toList()))
            .ifPresent(spot::setPhotoUrls);

        return spot;
    }

    private AttributeValue buildRangeKey(Spot spot) {
        String rangeKey = Stream.of(
            spot.getStatus().name(),
            spot.getCountry(),
            spot.getState(),
            spot.getCity(),
            spot.getName()
        )
            .map(val -> ofNullable(val).orElse(""))
            .map(String::toLowerCase)
            .collect(joining(SEPARATOR));

        return new AttributeValue(rangeKey);
    }

}
