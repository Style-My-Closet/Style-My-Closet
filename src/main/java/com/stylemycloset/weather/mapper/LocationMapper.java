package com.stylemycloset.weather.mapper;

import com.stylemycloset.location.Location;
import com.stylemycloset.weather.dto.WeatherAPILocation;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

    public WeatherAPILocation toDto(Location location) {
        if (location == null) {
            return null;
        }

        return new WeatherAPILocation(
            location.getLatitude(),
            location.getLongitude(),
            location.getX(),
            location.getY(),
            location.getLocationNames()
        );
    }

    public static Location toEntity(WeatherAPILocation dto) {
        if (dto == null) {
            return null;
        }

        return new Location(
            dto.latitude(),
            dto.longitude(),
            dto.x(),
            dto.y(),
            dto.locationNames()
        );
    }
}
