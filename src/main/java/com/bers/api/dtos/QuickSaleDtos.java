package com.bers.api.dtos;

import com.bers.domain.entities.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class QuickSaleDtos {

    public record QuickSaleRequest(
            @NotNull(message = "tripId is required")
            Long tripId,

            @NotBlank(message = "seatNumber is required")
            String seatNumber,

            @NotNull(message = "passengerId is required")
            Long passengerId,

            @NotNull(message = "fromStopId is required")
            Long fromStopId,

            @NotNull(message = "toStopId is required")
            Long toStopId,

            @NotNull(message = "paymentMethod is required")
            PaymentMethod paymentMethod,

            Boolean applyDiscount
    ) implements Serializable {
    }

    public record QuickSaleResponse(
            Long ticketId,
            String seatNumber,
            BigDecimal originalPrice,
            BigDecimal discount,
            BigDecimal finalPrice,
            String qrCode,
            LocalDateTime saleTime,
            Integer minutesUntilDeparture,
            String message
    ) implements Serializable {
    }

    public record AvailableQuickSaleSeatsResponse(
            Long tripId,
            String routeInfo,
            LocalDateTime departureTime,
            Integer minutesUntilDeparture,
            List<String> availableSeats,
            BigDecimal quickSalePrice
    ) implements Serializable {
    }
}
