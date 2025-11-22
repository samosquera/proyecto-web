package com.bers.services.service;

import com.bers.api.dtos.SeatHoldDtos.SeatHoldCreateRequest;
import com.bers.api.dtos.SeatHoldDtos.SeatHoldResponse;
import com.bers.api.dtos.SeatHoldDtos.SeatHoldUpdateRequest;

import java.util.List;

public interface SeatHoldService {

    SeatHoldResponse createSeatHold(SeatHoldCreateRequest request, Long userId);

    SeatHoldResponse updateSeatHold(Long id, SeatHoldUpdateRequest request);

    SeatHoldResponse getSeatHoldById(Long id);

    List<SeatHoldResponse> getAllSeatHolds();

    List<SeatHoldResponse> getSeatHoldsByTripId(Long tripId);

    List<SeatHoldResponse> getSeatHoldsByUserId(Long userId);

    List<SeatHoldResponse> getActiveSeatHoldsByTrip(Long tripId);

    void deleteSeatHold(Long id);

    void expireOldHolds();

    boolean isSeatHeld(Long tripId, String seatNumber);

    void releaseSeatHold(Long holdId);

    void convertHoldToTicket(Long holdId);
}
