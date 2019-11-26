package com.surf.advisor.spot.client.geo.api;

import com.surf.advisor.geolocation.api.model.Geolocation;
import com.surf.advisor.spot.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "geolocation", url = "${feign.geolocation.url}", configuration = FeignConfiguration.class)
public interface GeolocationApiClient {

    @PutMapping("/geolocation/geolocations")
    void putGeolocation(@RequestBody Geolocation request);

}
