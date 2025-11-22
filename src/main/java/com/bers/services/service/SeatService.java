package com.bers.services.service;

import com.bers.api.dtos.SeatDtos.*;
import com.bers.domain.entities.enums.SeatType;

import java.util.List;

public interface SeatService {

    SeatResponse createSeat(SeatCreateRequest request);

    SeatResponse updateSeat(Long id, SeatUpdateRequest request);

    SeatResponse getSeatById(Long id);

    SeatResponse getSeatByBusAndNumber(Long busId, String number);

    List<SeatResponse> getAllSeats();

    List<SeatResponse> getSeatsByBusId(Long busId);

    List<SeatResponse> getSeatsByBusIdAndType(Long busId, SeatType type);

    List<SeatStatusResponse> getAllTripSeatsClassified(Long tripId);

    SeatStatusBySegmentResponse getTripSeatsForSegment(
            Long tripId,
            Long fromStopId,
            Long toStopId);

    void deleteSeat(Long id);

    long countSeatsByBus(Long busId);

    void validateSeatNumber(Long busId, String number);
}
