package com.surf.advisor.spot.mapper;

import com.surf.advisor.spot.model.SpotRecord;
import com.surf.advisor.spot.util.RangeKeyUtils;
import com.surf.advisor.spot.web.api.model.CountryCode;
import com.surf.advisor.spot.web.api.model.Description;
import com.surf.advisor.spot.web.api.model.Spot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Mapper
public interface SpotMapper {

    SpotMapper INSTANCE = Mappers.getMapper(SpotMapper.class);

    Spot map(SpotRecord record);

    @Mapping(target = "rangeKey", expression = "java(computeRangeKey(spot))")
    SpotRecord map(Spot spot);

    default Map<String, String> map(List<Description> descriptions) {
        if (descriptions == null) {
            return null;
        }
        return descriptions.stream()
            .collect(toMap(e -> e.getLanguage().name(), Description::getContent));
    }

    default List<Description> map(Map<String, String> map) {
        if (map == null) {
            return null;
        }
        return map.entrySet().stream()
            .map(e -> new Description()
                .language(CountryCode.fromValue(e.getKey()))
                .content(e.getValue()))
            .collect(toList());
    }

    default String computeRangeKey(Spot spot) {
        return RangeKeyUtils.buildRangeKey(spot);
    }
}
