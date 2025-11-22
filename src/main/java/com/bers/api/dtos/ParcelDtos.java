package com.bers.api.dtos;

import com.bers.domain.entities.enums.ParcelStatus;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ParcelDtos {
    public record ParcelCreateRequest(
            @NotBlank String senderName,
            @NotBlank
            @Pattern(regexp = "\\d{10}", message = "phone must be exactly 10 digits")
            String senderPhone,
            @NotBlank String receiverName,
            @NotBlank
            @Pattern(regexp = "\\d{10}", message = "phone must be exactly 10 digits")
            String receiverPhone,
            @NotNull @DecimalMin("0.0")
            BigDecimal price,
            @NotNull Long fromStopId,
            @NotNull Long toStopId,
            Long tripId
    ) implements Serializable {
    }

    public record ParcelUpdateRequest(
            @NotNull(message = "status is required")
            ParcelStatus status,
            @Nullable
            String proofPhotoUrl,
            @NotNull(message = "Delivery otp is required")
            String deliveryOtp
    ) implements Serializable {
    }

    public record ParcelResponse(
            Long id,
            String code,
            String senderName,
            String senderPhone,
            String receiverName,
            String receiverPhone,
            BigDecimal price,
            String status,
            String proofPhotoUrl,
            String deliveryOtp,
            LocalDateTime createdAt,
            LocalDateTime deliveredAt,
            Long fromStopId,
            Long toStopId,
            Long tripId
    ) implements Serializable {
    }
}
