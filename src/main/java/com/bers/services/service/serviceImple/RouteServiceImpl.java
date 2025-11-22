package com.bers.services.service.serviceImple;

import com.bers.api.dtos.RouteDtos.RouteCreateRequest;
import com.bers.api.dtos.RouteDtos.RouteResponse;
import com.bers.api.dtos.RouteDtos.RouteUpdateRequest;
import com.bers.api.dtos.StopDtos.StopResponse;
import com.bers.domain.entities.Route;
import com.bers.domain.repositories.RouteRepository;
import com.bers.domain.repositories.StopRepository;
import com.bers.services.mappers.RouteMapper;
import com.bers.services.mappers.StopMapper;
import com.bers.services.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RouteServiceImpl implements RouteService {
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final RouteMapper routeMapper;
    private final StopMapper stopMapper;

    @Override
    public RouteResponse createRoute(RouteCreateRequest request) {
        if (routeRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Route code already exists: " + request.code());
        }

        Route route = routeMapper.toEntity(request);
        Route savedRoute = routeRepository.save(route);
        return routeMapper.toResponse(savedRoute);
    }

    @Override
    public RouteResponse updateRoute(Long id, RouteUpdateRequest request) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + id));

        routeMapper.updateEntity(request, route);
        Route updatedRoute = routeRepository.save(route);
        return routeMapper.toResponse(updatedRoute);
    }

    @Override
    @Transactional(readOnly = true)
    public RouteResponse getRouteById(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + id));
        return routeMapper.toResponse(route);
    }

    @Override
    @Transactional(readOnly = true)
    public RouteResponse getRouteByCode(String code) {
        Route route = routeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Route not found with code: " + code));
        return routeMapper.toResponse(route);
    }

    @Override
    @Transactional
    public RouteResponse getRouteWithStops(Long id) {
        Route route = routeRepository.findByIdWithStops(id)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + id));
        return routeMapper.toResponse(route);
    }

    @Override
    @Transactional
    public List<RouteResponse> getAllRoutes() {
        return routeRepository.findAll().stream()
                .map(routeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllOrigins() {
        return routeRepository.getAllOrigins();
    }

    @Override
    public List<String> filterDestinationsByOrigin(String origin) {
        return routeRepository.getDestinationsByOrigin(origin);
    }

    @Override
    @Transactional
    public List<RouteResponse> searchRoutes(String origin, String destination) {
        if (origin != null && destination != null) {
            return routeRepository.findByOriginIgnoreCaseAndDestinationIgnoreCase(origin, destination).stream()
                    .map(routeMapper::toResponse)
                    .collect(Collectors.toList());
        } else if (origin != null || destination != null) {
            String searchTerm = origin != null ? origin : destination;
            return routeRepository.findByOriginContainingIgnoreCaseOrDestinationContainingIgnoreCase(
                            searchTerm, searchTerm).stream()
                    .map(routeMapper::toResponse)
                    .collect(Collectors.toList());
        }
        return getAllRoutes();
    }

    @Override
    @Transactional
    public List<StopResponse> getStopsByRoute(Long routeId) {
        if (!routeRepository.existsById(routeId)) {
            throw new IllegalArgumentException("Route not found: " + routeId);
        }
        return stopRepository.findByRouteIdOrderByOrderAsc(routeId).stream()
                .map(stopMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRoute(Long id) {
        if (!routeRepository.existsById(id)) {
            throw new IllegalArgumentException("Route not found: " + id);
        }
        routeRepository.deleteById(id);
    }

    @Override
    @Transactional
    public boolean existsByCode(String code) {
        return routeRepository.existsByCode(code);
    }
}
