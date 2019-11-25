package com.surf.advisor.spot.service;

import com.surf.advisor.spot.mapper.SpotMapper;
import com.surf.advisor.spot.model.SpotQueryProps;
import com.surf.advisor.spot.repository.ISpotRepository;
import com.surf.advisor.spot.web.api.SpotsApiDelegate;
import com.surf.advisor.spot.web.api.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import static com.surf.advisor.spot.web.api.model.SpotStatus.DRAFT;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class SpotApiDelegateImpl implements SpotsApiDelegate {

    private final ISpotRepository spotRepository;

    @Override
    public ResponseEntity<SpotIdResponse> postSpot(Spot spot) {
        spot.setId(randomUUID().toString());
        spot.setStatus(DRAFT);

        return putSpot(spot);
    }

    @Override
    public ResponseEntity<SpotIdResponse> putSpot(Spot spot) {
        spotRepository.put(SpotMapper.INSTANCE.map(spot));

        return ResponseEntity.ok(new SpotIdResponse().id(spot.getId()));
    }

    @Override
    public ResponseEntity<Spot> getSpot(String id) {
        return spotRepository.get(id)
            .map(SpotMapper.INSTANCE::map)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new HttpClientErrorException(NOT_FOUND));
    }

    @Override
    public ResponseEntity<PagedSpotResponse> findSpots(Integer limit, SpotFilters filters,
                                                       String lastKey) {

        var props = new SpotQueryProps(filters, limit, lastKey);

        var page = spotRepository.findSpots(props);

        return ResponseEntity.ok(page);
    }

    @Override
    public ResponseEntity<SpotIdListResponse> filterSpotIds(SpotFilters filters) {

        return ofNullable(filters.getIds())
            .map(ids -> new SpotQueryProps(filters, ids.size(), null))
            .map(spotRepository::findSpotIds)
            .map(filteredIds -> new SpotIdListResponse().ids(filteredIds))
            .map(ResponseEntity::ok)
            .orElseThrow();
    }
}
