package com.bers.services.service;

import com.bers.api.dtos.RouteDtos.RouteCreateRequest;
import com.bers.api.dtos.RouteDtos.RouteResponse;
import com.bers.api.dtos.RouteDtos.RouteUpdateRequest;
import com.bers.api.dtos.StopDtos.StopResponse;

import java.util.List;

public interface RouteService {

    RouteResponse createRoute(RouteCreateRequest request);

    RouteResponse updateRoute(Long id, RouteUpdateRequest request);

    RouteResponse getRouteById(Long id);

    RouteResponse getRouteByCode(String code);

    RouteResponse getRouteWithStops(Long id);

    List<RouteResponse> getAllRoutes();

    List<String> getAllOrigins();

    List<String> filterDestinationsByOrigin(String origin);

    List<RouteResponse> searchRoutes(String origin, String destination);

    List<StopResponse> getStopsByRoute(Long routeId);

    void deleteRoute(Long id);

    boolean existsByCode(String code);
}
