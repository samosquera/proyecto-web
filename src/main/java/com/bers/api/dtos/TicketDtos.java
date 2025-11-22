package com.bers.api.dtos;

import com.bers.domain.entities.enums.CancellationPolicy;
import com.bers.domain.entities.enums.PassengerType;
import com.bers.domain.entities.enums.PaymentMethod;
import com.bers.domain.entities.enums.TicketStatus;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TicketDtos {
    public record TicketCreateRequest(
            @NotNull(message = "tripId is required")
            Long tripId,
            @NotNull(message = "passengerId is required")
            Long passengerId,
            @NotNull(message = "fromStopId is required")
            Long fromStopId,
            @NotNull(message = "toStopId is required")
            Long toStopId,
            @NotBlank(message = "seatNumber is required")
            @Size(max = 10, message = "seatNumber must not exceed 10 characters")
            String seatNumber,
            @NotNull(message = "paymentMethod is required")
            PaymentMethod paymentMethod
    ) implements Serializable {
    }

    public record TicketUpdateRequest(
            @NotNull(message = "status is required")
            TicketStatus status
    ) implements Serializable {
    }

    public record TicketPaymentConfirmRequest(
            @NotNull(message = "paymentMethod is required")
            PaymentMethod paymentMethod
    ) implements Serializable {
    }

    public record TicketResponse(
            Long id,
            Long tripId,
            String tripDate,
            String tripTime,
            Long passengerId,
            String passengerName,
            Long fromStopId,
            String fromStopName,
            Long toStopId,
            String toStopName,
            String seatNumber,
            BigDecimal price,
            String paymentMethod,
            String status,
            String qrCode,
            LocalDateTime createdAt,
            @Nullable
            LocalDateTime cancelledAt,
            @Nullable
            BigDecimal refundAmount,
            CancellationPolicy cancellationPolicy,
            String routeOrigin,
            String routeDestination,
            String busPlate
    ) implements Serializable {
    }

    public record TicketSummaryResponse(
            Long id,
            String seatNumber,
            BigDecimal price,
            String qrCode,
            String tripDate,
            String tripTime,
            String fromStopName,
            String toStopName,
            String passengerName
    ) implements Serializable {
    }

    public record TicketAdminResponse(
            Long id,
            Long tripId,
            String passengerName,
            String seatNumber,
            BigDecimal price,
            PassengerType passengerType,
            BigDecimal discountAmount,
            PaymentMethod paymentMethod,
            TicketStatus status,
            LocalDateTime createdAt,
            LocalDateTime cancelledAt,
            BigDecimal refundAmount,
            CancellationPolicy cancellationPolicy
    ) implements Serializable {
    }
}
