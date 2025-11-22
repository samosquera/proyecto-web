package com.bers.services.mappers;

import com.bers.api.dtos.SeatHoldDtos.SeatHoldCreateRequest;
import com.bers.api.dtos.SeatHoldDtos.SeatHoldResponse;
import com.bers.api.dtos.SeatHoldDtos.SeatHoldUpdateRequest;
import com.bers.domain.entities.SeatHold;
import com.bers.domain.entities.Trip;
import org.mapstruct.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface SeatHoldMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "status", expression = "java(com.bers.domain.entities.enums.HoldStatus.HOLD)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "seatNumber", source = "seatNumber")
    @Mapping(target = "trip", source = "tripId", qualifiedByName = "mapTrip")
    @Mapping(target = "fromStop", ignore = true)
    @Mapping(target = "toStop", ignore = true)
    SeatHold toEntity(SeatHoldCreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "seatNumber", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "fromStop", ignore = true)
    @Mapping(target = "toStop", ignore = true)
    void updateEntity(SeatHoldUpdateRequest dto, @MappingTarget SeatHold entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "seatNumber", source = "seatNumber")
    @Mapping(target = "expiresAt", source = "expiresAt")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "tripId", source = "trip.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "tripDate", source = "trip.date", qualifiedByName = "formatDate")
    @Mapping(target = "tripTime", source = "trip.departureAt", qualifiedByName = "formatTime")
    @Mapping(target = "routeName", source = "trip.route.name")
    @Mapping(target = "minutesLeft", source = "expiresAt", qualifiedByName = "calculateMinutesLeft")
    SeatHoldResponse toResponse(SeatHold entity);


    @Named("formatDate")
    default String formatDate(java.time.LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }

    @Named("formatTime")
    default String formatTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern("HH:mm")) : null;
    }

    @Named("calculateMinutesLeft")
    default Integer calculateMinutesLeft(LocalDateTime expiresAt) {
        if (expiresAt == null) return null;
        long minutes = Duration.between(LocalDateTime.now(), expiresAt).toMinutes();
        return minutes > 0 ? (int) minutes : 0;
    }

    @Named("mapTrip")
    default Trip mapTrip(Long id) {
        if (id == null) return null;
        Trip t = new Trip();
        t.setId(id);
        return t;
    }
}