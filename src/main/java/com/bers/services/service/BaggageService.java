package com.bers.services.service;

import com.bers.api.dtos.BaggageDtos.BaggageCreateRequest;
import com.bers.api.dtos.BaggageDtos.BaggageResponse;
import com.bers.api.dtos.BaggageDtos.BaggageUpdateRequest;

import java.math.BigDecimal;
import java.util.List;

public interface BaggageService {

    BaggageResponse createBaggage(BaggageCreateRequest request);

    BaggageResponse updateBaggage(Long id, BaggageUpdateRequest request);

    BaggageResponse getBaggageById(Long id);

    BaggageResponse getBaggageByTagCode(String tagCode);

    List<BaggageResponse> getAllBaggage();

    List<BaggageResponse> getBaggageByTicketId(Long ticketId);

    List<BaggageResponse> getBaggageByTripId(Long tripId);

    void deleteBaggage(Long id);

    BigDecimal calculateBaggageFee(BigDecimal weightKg);

    BigDecimal getTotalWeightByTrip(Long tripId);
}
