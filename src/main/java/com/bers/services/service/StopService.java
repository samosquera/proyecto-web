package com.bers.services.service;

import com.bers.api.dtos.StopDtos.StopCreateRequest;
import com.bers.api.dtos.StopDtos.StopResponse;
import com.bers.api.dtos.StopDtos.StopUpdateRequest;

import java.util.List;

public interface StopService {

    StopResponse createStop(StopCreateRequest request);

    StopResponse updateStop(Long id, StopUpdateRequest request);

    StopResponse getStopById(Long id);

    List<StopResponse> getAllStops();

    List<StopResponse> getAvailableDestinations(Long fromStopId);

    List<StopResponse> getStopsByRouteId(Long routeId);

    List<StopResponse> searchStopsByName(String name);

    void deleteStop(Long id);

    void validateStopOrder(Long routeId, Integer order);
}
