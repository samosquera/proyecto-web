package com.bers.services.mappers;

import com.bers.api.dtos.AssignmentDtos.AssignmentCreateRequest;
import com.bers.api.dtos.AssignmentDtos.AssignmentResponse;
import com.bers.api.dtos.AssignmentDtos.AssignmentUpdateRequest;
import com.bers.domain.entities.Assignment;
import com.bers.domain.entities.Route;
import com.bers.domain.entities.Trip;
import com.bers.domain.entities.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AssignmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "checklistOk", constant = "false")
    @Mapping(target = "assignedAt", ignore = true)
    @Mapping(target = "trip", source = "tripId", qualifiedByName = "mapTrip")
    @Mapping(target = "driver", source = "driverId", qualifiedByName = "mapUser")
    @Mapping(target = "dispatcher", source = "dispatcherId", qualifiedByName = "mapUser")
    Assignment toEntity(AssignmentCreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedAt", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "dispatcher", ignore = true)
    @Mapping(target = "checklistOk", source = "checklistOk")
    void updateEntity(AssignmentUpdateRequest dto, @MappingTarget Assignment entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "checklistOk", source = "checklistOk")
    @Mapping(target = "assignedAt", source = "assignedAt")
    @Mapping(target = "tripId", source = "trip.id")
    @Mapping(target = "tripInfo", source = "trip", qualifiedByName = "formatTripInfo")
    @Mapping(target = "tripStatus", source = "trip.status")
    @Mapping(target = "tripDate", source = "trip.date")
    @Mapping(target = "tripDepartureTime", source = "trip.departureAt")
    @Mapping(target = "routeInfo", source = "trip.route", qualifiedByName = "formatRouteInfo")
    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "driverName", source = "driver.username")
    @Mapping(target = "dispatcherId", source = "dispatcher.id")
    @Mapping(target = "dispatcherName", source = "dispatcher.username")
    AssignmentResponse toResponse(Assignment entity);

    @Named("mapTrip")
    default Trip mapTrip(Long id) {
        if (id == null) return null;
        Trip t = new Trip();
        t.setId(id);
        return t;
    }

    @Named("mapUser")
    default User mapUser(Long id) {
        if (id == null) return null;
        User u = new User();
        u.setId(id);
        return u;
    }

    @Named("formatTripInfo")
    default String formatTripInfo(Trip trip) {
        if (trip == null || trip.getRoute() == null) return null;
        return trip.getRoute().getOrigin() + " → " +
                trip.getRoute().getDestination() + " - " +
                trip.getDate() + " " +
                trip.getDepartureAt().toLocalTime();
    }

    @Named("formatRouteInfo")
    default String formatRouteInfo(Route route) {
        if (route == null) return null;
        return route.getOrigin() + " → " + route.getDestination();
    }
}