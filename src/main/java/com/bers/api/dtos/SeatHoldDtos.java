package com.bers.api.dtos;

import com.bers.domain.entities.enums.HoldStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SeatHoldDtos {

    public record SeatHoldCreateRequest(
            @NotNull(message = "tripId is required")
            Long tripId,
            @NotBlank(message = "seatNumber is required")
            @Size(max = 10, message = "seatNumber must not exceed 10 characters")
            String seatNumber,
            @NotNull(message = "fromStopId is required")
            Long fromStopId,
            @NotNull(message = "toStopId is required")
            Long toStopId
    ) implements Serializable {
    }

    public record SeatHoldUpdateRequest(
            @NotNull(message = "status is required")
            HoldStatus status
    ) implements Serializable {
    }

    public record SeatHoldResponse(
            Long id,
            String seatNumber,
            LocalDateTime expiresAt,
            String status,
            LocalDateTime createdAt,
            Long tripId,
            Long userId,
            String tripDate,
            String tripTime,
            String routeName,
            Integer minutesLeft
    ) implements Serializable {
    }

    public record SeatHoldSegment(
            @NotNull
            Long seatHoldId,

            @NotNull
            Long startHold,

            @NotNull
            Long finishHold
    ) implements Serializable {
    }
}
