package com.surf.advisor.spot.model;

import static java.util.Optional.ofNullable;

import com.surf.advisor.spot.web.api.model.SpotFilters;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpotFilterColumn {

    STATUS(SpotRecord.STATUS,
        s -> ofNullable(s.getStatus()).filter(list -> list.size() == 1)
            .map(list -> list.get(0)).map(Enum::name).orElse(null)),

    COUNTRY(SpotRecord.COUNTRY, SpotFilters::getCountry),
    STATE(SpotRecord.STATE, SpotFilters::getState),
    CITY(SpotRecord.CITY, SpotFilters::getCity),
    NAME(SpotRecord.NAME, SpotFilters::getName)

    ;
    private final String name;
    private final Function<SpotFilters, String> getter;

}
