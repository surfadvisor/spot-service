package com.surf.advisor.spot.repository;

import com.surf.advisor.spot.model.SpotQueryProps;
import com.surf.advisor.spot.model.SpotRecord;
import com.surf.advisor.spot.web.api.model.PagedSpotResponse;
import java.util.List;

public interface ISpotRepository {

    void put(SpotRecord spotRecord);

    PagedSpotResponse findSpots(SpotQueryProps queryProps);

    List<String> findSpotIds(SpotQueryProps queryProps);

}
