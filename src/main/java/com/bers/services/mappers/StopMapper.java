package com.bers.services.mappers;

import com.bers.api.dtos.StopDtos.StopCreateRequest;
import com.bers.api.dtos.StopDtos.StopResponse;
import com.bers.api.dtos.StopDtos.StopUpdateRequest;
import com.bers.domain.entities.Route;
import com.bers.domain.entities.Stop;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface StopMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "order", source = "order")
    @Mapping(target = "lat", source = "lat")
    @Mapping(target = "lng", source = "lng")
    @Mapping(target = "route", source = "routeId", qualifiedByName = "mapRoute")
    Stop toEntity(StopCreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lat", ignore = true)
    @Mapping(target = "lng", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "order", source = "order")
    void updateEntity(StopUpdateRequest dto, @MappingTarget Stop stop);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "order", source = "order")
    @Mapping(target = "lat", source = "lat")
    @Mapping(target = "lng", source = "lng")
    @Mapping(target = "routeId", source = "route.id")
    @Mapping(target = "routeName", source = "route.name")
    @Mapping(target = "routeCode", source = "route.code")
    StopResponse toResponse(Stop entity);

    @Named("mapRoute")
    default Route mapRoute(Long id) {
        if (id == null) return null;
        Route r = new Route();
        r.setId(id);
        return r;
    }
}