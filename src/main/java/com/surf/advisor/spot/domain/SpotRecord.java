package com.surf.advisor.spot.domain;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.surf.advisor.spot.web.api.model.Spot;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class SpotRecord {

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String PHOTO_URLS = "photoUrls";
    private static final String STATUS = "status";

    private Map<String, AttributeValue> values = new HashMap<>();

    public SpotRecord(Long id, String name, List<String> photoUrs, String status) {
        values.put(ID, new AttributeValue().withN(valueOf(id)));
        values.put(NAME, new AttributeValue(name));
        values.put(STATUS, new AttributeValue(status));

        ofNullable(photoUrs).ifPresent(urls -> {
            List<AttributeValue> urlList = urls.stream().map(AttributeValue::new).collect(toList());

            values.put(PHOTO_URLS, new AttributeValue().withL(urlList));
        });
    }

    public SpotRecord(Spot spot) {
        this(spot.getId(), spot.getName(), spot.getPhotoUrls(), spot.getStatus().name());
    }

    public SpotRecord(Map<String, AttributeValue> values) {
        this.values.putAll(values);
    }

    public Spot toResponse() {
        var spot = new Spot();

        spot.setId(Long.valueOf(values.get(ID).getN()));
        spot.setName(values.get(NAME).getS());
        spot.setStatus(Spot.StatusEnum.fromValue(values.get(STATUS).getS()));

        ofNullable(values.get(PHOTO_URLS))
            .map(urls -> urls.getL().stream().map(AttributeValue::getS).collect(toList()))
            .ifPresent(spot::setPhotoUrls);

        return spot;
    }

}
