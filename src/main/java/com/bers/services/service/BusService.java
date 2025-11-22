package com.bers.services.service;

import com.bers.api.dtos.BusDtos.BusCreateRequest;
import com.bers.api.dtos.BusDtos.BusResponse;
import com.bers.api.dtos.BusDtos.BusUpdateRequest;
import com.bers.domain.entities.enums.BusStatus;

import java.util.List;

public interface BusService {

    BusResponse createBus(BusCreateRequest request);

    BusResponse updateBus(Long id, BusUpdateRequest request);

    BusResponse getBusById(Long id);

    BusResponse getBusWithSeats(Long id);

    BusResponse getBusbyPlate(String plate);

    List<BusResponse> getAllBuses();

    List<BusResponse> getBusesByStatus(BusStatus status);

    List<BusResponse> getAvailableBuses(Integer minCapacity);

    void deleteBus(Long id);

    boolean existsByPlate(String plate);

    BusResponse changeBusStatus(Long id, BusStatus status);
}
