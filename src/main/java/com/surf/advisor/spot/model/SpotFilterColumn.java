package com.surf.advisor.spot.model;

import com.surf.advisor.spot.web.api.model.SpotFilters;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

import static java.util.Optional.ofNullable;

@Getter
@RequiredArgsConstructor
public enum SpotFilterColumn {

    STATUS(SpotRecord.STATUS, s -> ofNullable(s.getStatus()).map(Enum::name).orElse(null)),
    //TODO: take all countries into account
    COUNTRY(SpotRecord.COUNTRY, s -> ofNullable(s.getCountry()).map(l -> l.get(0)).map(Enum::name).orElse(null)),
    STATE(SpotRecord.STATE, SpotFilters::getState),
    CITY(SpotRecord.CITY, SpotFilters::getCity),
    NAME(SpotRecord.NAME, SpotFilters::getName)

    ;
    private final String name;
    private final Function<SpotFilters, String> getter;

}
