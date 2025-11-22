package com.bers.api.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class RouteDtos {

    public record RouteCreateRequest(
            @NotBlank(message = "code is required")
            String code,
            @NotBlank(message = "name is required")
            String name,
            @NotBlank(message = "origin is required")
            String origin,
            @NotBlank(message = "destination is required")
            String destination,
            @NotNull @Min(1)
            Integer distanceKm,
            @NotNull @Min(1)
            Integer durationMin
    ) implements Serializable {
    }

    public record RouteUpdateRequest(
            @NotBlank String name,
            @Min(1) Integer distanceKm,
            @Min(1) Integer durationMin
    ) implements Serializable {
    }

    public record RouteResponse(
            Long id,
            String code,
            String name,
            String origin,
            String destination,
            Integer distanceKm,
            Integer durationMin,
            List<StopSummary> stops
    ) implements Serializable {
    }

    public record StopSummary(
            Long id,
            String name,
            Integer order,
            BigDecimal lat,
            BigDecimal lng
    ) {
    }

}
