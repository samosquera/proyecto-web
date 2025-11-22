package com.bers.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

public class OverbookingDtos {

    public record OverbookingCreateRequest(
            @NotNull(message = "tripId is required")
            Long tripId,

            @NotNull(message = "ticketId is required")
            Long ticketId,

            @NotBlank(message = "reason is required")
            String reason
    ) implements Serializable {
    }

    public record OverbookingApproveRequest(
            String notes
    ) implements Serializable {
    }

    public record OverbookingRejectRequest(
            @NotBlank(message = "reason is required")
            String reason
    ) implements Serializable {
    }

    public record OverbookingResponse(
            Long id,
            Long tripId,
            String tripInfo,
            Long ticketId,
            String passengerName,
            String seatNumber,
            String status,
            String reason,
            String requestedByName,
            String approvedByName,
            LocalDateTime requestedAt,
            LocalDateTime approvedAt,
            LocalDateTime expiresAt,
            boolean requiresApproval,
            double currentOccupancyRate,
            int minutesUntilDeparture
    ) implements Serializable {
    }
}