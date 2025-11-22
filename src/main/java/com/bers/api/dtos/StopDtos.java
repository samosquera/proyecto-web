package com.bers.api.dtos;

import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.math.BigDecimal;

public class StopDtos {
    public record StopCreateRequest(
            @NotBlank(message = "name is required")
            String name,
            @NotNull @Min(0)
            Integer order,
            @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
            @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
            BigDecimal lat,
            @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
            @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
            BigDecimal lng,
            @NotNull(message = "routeId is required")
            Long routeId
    ) implements Serializable {
    }

    public record StopUpdateRequest(
            @NotBlank String name,
            @Min(0) Integer order
    ) implements Serializable {
    }

    public record StopResponse(
            Long id,
            String name,
            Integer order,
            BigDecimal lat,
            BigDecimal lng,
            Long routeId,
            String routeName,
            String routeCode
    ) implements Serializable {
    }
}
