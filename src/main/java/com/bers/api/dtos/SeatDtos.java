package com.bers.api.dtos;

import com.bers.domain.entities.enums.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;

public class SeatDtos {
    public record SeatCreateRequest(
            @NotBlank(message = "number is required")
            @Size(max = 10, message = "number must not exceed 10 characters")
            String number,
            @NotNull(message = "type is required")
            SeatType type,
            @NotNull(message = "busId is required")
            Long busId
    ) implements Serializable {
    }

    public record SeatUpdateRequest(
            @NotNull(message = "type is required")
            SeatType type
    ) implements Serializable {
    }

    public record SeatResponse(
            Long id,
            String number,
            String type,
            Long busId,
            String busPlate,
            Integer busCapacity
    ) implements Serializable {
    }

    public record SeatStatusResponse(
            Long seatId,
            String seatNumber,
            boolean isOccupied,  // Ocupado con ticket SOLD
            boolean isHeld,      // En hold activo
            boolean available    // Disponible para reservar
    ) implements Serializable {
    }

    public record SegmentInfo(
            Long fromStopId,
            String fromStopName,
            Integer fromStopOrder,
            Long toStopId,
            String toStopName,
            Integer toStopOrder
    ) implements Serializable {
    }

    public record SeatStatusBySegmentResponse(
            SegmentInfo segment,
            List<SeatStatusResponse> seats
    ) implements Serializable {
    }

    public record SegmentKey(
            Long fromStopId,
            String fromStopName,
            Integer fromStopOrder,
            Long toStopId,
            String toStopName,
            Integer toStopOrder
    ) {
    }


}