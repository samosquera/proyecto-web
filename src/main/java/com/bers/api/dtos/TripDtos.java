package com.bers.api.dtos;

import com.bers.domain.entities.enums.TripStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TripDtos {
    public record TripCreateRequest(
            @NotNull(message = "date is required")
            @FutureOrPresent(message = "date must be today or in the future")
            LocalDate date,

            @NotNull(message = "departureAt is required")
            LocalDateTime departureAt,

            @NotNull(message = "arrivalEta is required")
            LocalDateTime arrivalEta,

            @NotNull(message = "routeId is required")
            Long routeId,

            Long busId
    ) implements Serializable {
    }

    public record TripUpdateRequest(
            @NotNull(message = "departureAt is required")
            LocalDateTime departureAt,
            @NotNull(message = "arrivalEta is required")
            LocalDateTime arrivalEta,
            Long busId,
            @NotNull(message = "status is required")
            TripStatus status
    ) implements Serializable {
    }

    public record TripResponse(
            Long id,
            LocalDate date,
            LocalDateTime departureAt,
            LocalDateTime arrivalEta,
            String status,
            Long routeId,
            String routeName,
            String origin,
            String destination,
            Long busId,
            String busPlate,
            Integer capacity
    ) implements Serializable {
    }
}
