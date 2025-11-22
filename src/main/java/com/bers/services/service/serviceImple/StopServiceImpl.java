package com.bers.services.service.serviceImple;

import com.bers.api.dtos.StopDtos.StopCreateRequest;
import com.bers.api.dtos.StopDtos.StopResponse;
import com.bers.api.dtos.StopDtos.StopUpdateRequest;
import com.bers.domain.entities.Route;
import com.bers.domain.entities.Stop;
import com.bers.domain.repositories.RouteRepository;
import com.bers.domain.repositories.StopRepository;
import com.bers.services.mappers.StopMapper;
import com.bers.services.service.StopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StopServiceImpl implements StopService {
    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;
    private final StopMapper stopMapper;

    @Override
    public StopResponse createStop(StopCreateRequest request) {
        Route route = routeRepository.findById(request.routeId())
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + request.routeId()));

        validateStopOrder(request.routeId(), request.order());

        Stop stop = stopMapper.toEntity(request);
        stop.setRoute(route);

        Stop savedStop = stopRepository.save(stop);
        return stopMapper.toResponse(savedStop);
    }

    @Override
    public StopResponse updateStop(Long id, StopUpdateRequest request) {
        Stop stop = stopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stop not found: " + id));

        if (!stop.getOrder().equals(request.order())) {
            validateStopOrder(stop.getRoute().getId(), request.order());
        }

        stopMapper.updateEntity(request, stop);
        Stop updatedStop = stopRepository.save(stop);
        return stopMapper.toResponse(updatedStop);
    }

    @Override
    @Transactional
    public StopResponse getStopById(Long id) {
        Stop stop = stopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stop not found: " + id));
        return stopMapper.toResponse(stop);
    }

    @Override
    @Transactional
    public List<StopResponse> getAllStops() {
        return stopRepository.findAll().stream()
                .map(stopMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<StopResponse> getAvailableDestinations(Long fromStopId) {
        // Obtiene la parada de origen
        Stop fromStop = stopRepository.findById(fromStopId)
                .orElseThrow(() -> new IllegalArgumentException("Stop not found: " + fromStopId));

        // Obtiene las paradas de la misma ruta con order mayor al de la parada de origen
        List<Stop> availableStops = stopRepository
                .findByRoute_IdAndOrderGreaterThanOrderByOrderAsc(fromStop.getRoute().getId(), fromStop.getOrder());

        // Convierte a StopResponse usando el mapper
        return availableStops.stream()
                .map(stopMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<StopResponse> getStopsByRouteId(Long routeId) {
        if (!routeRepository.existsById(routeId)) {
            throw new IllegalArgumentException("Route not found: " + routeId);
        }
        return stopRepository.findByRouteIdOrderByOrderAsc(routeId).stream()
                .map(stopMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<StopResponse> searchStopsByName(String name) {
        return stopRepository.findByNameContainingIgnoreCase(name).stream()
                .map(stopMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteStop(Long id) {
        if (!stopRepository.existsById(id)) {
            throw new IllegalArgumentException("Stop not found: " + id);
        }
        stopRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void validateStopOrder(Long routeId, Integer order) {
        if (order < 0) {
            throw new IllegalArgumentException("Stop order must be non-negative");
        }

        List<Stop> existingStops = stopRepository.findByRouteIdOrderByOrderAsc(routeId);
        boolean orderExists = existingStops.stream()
                .anyMatch(stop -> stop.getOrder().equals(order));

        if (orderExists) {
            throw new IllegalArgumentException("Stop order " + order + " already exists for route " + routeId);
        }
    }

}
