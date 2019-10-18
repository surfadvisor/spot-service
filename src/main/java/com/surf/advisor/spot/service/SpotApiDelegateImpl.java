package com.surf.advisor.spot.service;

import static com.surf.advisor.spot.web.api.model.Spot.StatusEnum.DRAFT;
import static java.util.UUID.randomUUID;

import com.surf.advisor.spot.domain.SpotRecord;
import com.surf.advisor.spot.repository.ISpotRepository;
import com.surf.advisor.spot.web.api.SpotsApiDelegate;
import com.surf.advisor.spot.web.api.model.Spot;
import com.surf.advisor.spot.web.api.model.SpotIdResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
        spotRepository.put(new SpotRecord(spot));

        return ResponseEntity.ok(new SpotIdResponse().id(spot.getId()));
    }
}