package com.bers.services.mappers;

import com.bers.api.dtos.RouteDtos.RouteCreateRequest;
import com.bers.api.dtos.RouteDtos.RouteResponse;
import com.bers.api.dtos.RouteDtos.RouteUpdateRequest;
import com.bers.api.dtos.RouteDtos.StopSummary;
import com.bers.domain.entities.Route;
import com.bers.domain.entities.Stop;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RouteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stops", ignore = true)
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "origin", source = "origin")
    @Mapping(target = "destination", source = "destination")
    @Mapping(target = "distanceKm", source = "distanceKm")
    @Mapping(target = "durationMin", source = "durationMin")
    Route toEntity(RouteCreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "destination", ignore = true)
    @Mapping(target = "stops", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "distanceKm", source = "distanceKm")
    @Mapping(target = "durationMin", source = "durationMin")
    void updateEntity(RouteUpdateRequest dto, @MappingTarget Route route);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "origin", source = "origin")
    @Mapping(target = "destination", source = "destination")
    @Mapping(target = "distanceKm", source = "distanceKm")
    @Mapping(target = "durationMin", source = "durationMin")
    @Mapping(target = "stops", source = "stops", qualifiedByName = "mapStopsToSummary")
    RouteResponse toResponse(Route entity);

    @Named("mapStopsToSummary")
    default List<StopSummary> mapStopsToSummary(List<Stop> stops) {
        if (stops == null) return null;
        return stops.stream()
                .map(stop -> new StopSummary(
                        stop.getId(),
                        stop.getName(),
                        stop.getOrder(),
                        stop.getLat(),
                        stop.getLng()
                ))
                .collect(Collectors.toList());
    }
}