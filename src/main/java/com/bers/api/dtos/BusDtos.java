package com.bers.api.dtos;

import com.bers.domain.entities.enums.BusStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Map;

public class BusDtos {
    public record BusCreateRequest(
            @NotBlank(message = "plate is required")
            @Size(max = 20, message = "plate must not exceed 20 characters")
            String plate,
            @NotNull(message = "capacity is required")
            @Min(value = 1, message = "capacity must be at least 1")
            Integer capacity,
            Map<String, Object> amenities,
            @NotNull(message = "status is required")
            BusStatus status
    ) implements Serializable {
    }

    public record BusUpdateRequest(
            @Min(value = 1, message = "Capacity must be at least 1")
            Integer capacity,
            Map<String, Object> amenities,
            @NotNull BusStatus status
    ) implements Serializable {
    }

    public record BusResponse(
            Long id,
            String plate,
            Integer capacity,
            Map<String, Object> amenities,
            String status
    ) implements Serializable {
    }

    public record BusWithSeatsResponse(
            Long id,
            String plate,
            Integer capacity,
            Map<String, Object> amenities,
            String status,
            Integer totalSeats,
            Integer availableSeats
    ) implements Serializable {
    }
}