package com.stylemycloset.location.mapper;

import com.stylemycloset.location.Location;
import com.stylemycloset.weather.dto.WeatherAPILocation;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

    public WeatherAPILocation toDto(Location location) {

        return new WeatherAPILocation(
            location.getLatitude(),
            location.getLongitude(),
            location.getX(),
            location.getY(),
            location.getLocationNames()
        );
    }

    public Location toEntity(WeatherAPILocation dto) {
        return new Location(
            dto.latitude(),
            dto.longitude(),
            dto.x(),
            dto.y(),
            dto.locationNames()
        );
    }
}
