package com.bers.api.controllers;

import com.bers.api.dtos.RouteDtos.RouteCreateRequest;
import com.bers.api.dtos.RouteDtos.RouteResponse;
import com.bers.api.dtos.RouteDtos.RouteUpdateRequest;
import com.bers.api.dtos.StopDtos.StopResponse;
import com.bers.services.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
@Slf4j
public class RouteController {

    private final RouteService routeService;

    @GetMapping("/all")
    public ResponseEntity<List<RouteResponse>> getAllRoutes() {
        log.debug("Retrieving all routes");

        List<RouteResponse> routes = routeService.getAllRoutes();
        return ResponseEntity.ok(routes);
    }

    // Listar todos los origins disponibles de rutas ofrecidas
    @GetMapping("/origins")
    public ResponseEntity<List<String>> getAllOrigins() {
        List<String> origins = routeService.getAllOrigins();
        return ResponseEntity.ok(origins);
    }

    //Filtrar destinos para un origen dado
    @GetMapping("/destinations")
    public ResponseEntity<List<String>> getDestinationsByOrigin(
            @RequestParam String origin
    ) {
        List<String> destinations = routeService.filterDestinationsByOrigin(origin);
        return ResponseEntity.ok(destinations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> getRouteById(@PathVariable Long id) {
        log.debug("Retrieving route: {}", id);

        RouteResponse route = routeService.getRouteById(id);
        return ResponseEntity.ok(route);
    }

    @GetMapping("/{id}/stops")
    public ResponseEntity<List<StopResponse>> getRouteStops(@PathVariable Long id) {
        log.debug("Retrieving stops for route: {}", id);

        List<StopResponse> stops = routeService.getStopsByRoute(id);
        return ResponseEntity.ok(stops);
    }

    @GetMapping("/search")
    public ResponseEntity<List<RouteResponse>> searchRoutes(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination
    ) {
        log.debug("Searching routes - origin: {}, destination: {}", origin, destination);

        List<RouteResponse> routes = routeService.searchRoutes(origin, destination);
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<RouteResponse> getRouteByCode(@PathVariable String code) {
        log.debug("Retrieving route by code: {}", code);

        RouteResponse route = routeService.getRouteByCode(code);
        return ResponseEntity.ok(route);
    }

    @GetMapping("/{id}/with-stops")
    public ResponseEntity<RouteResponse> getRouteWithStops(@PathVariable Long id) {
        log.debug("Retrieving route with stops: {}", id);

        RouteResponse route = routeService.getRouteWithStops(id);
        return ResponseEntity.ok(route);
    }

    // ==================== DISPATCHER/ADMIN ENDPOINTS ====================

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<RouteResponse> createRoute(@Valid @RequestBody RouteCreateRequest request) {
        log.info("adding new route: {} -> {}", request.origin(), request.destination());

        RouteResponse created = routeService.createRoute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<RouteResponse> updateRoute(
            @PathVariable Long id,
            @Valid @RequestBody RouteUpdateRequest request
    ) {
        log.info("Updating route: {} -> {}", id, request.name());

        RouteResponse updated = routeService.updateRoute(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        log.warn("Deleting route: {}", id);

        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/code/{code}/exists")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<Boolean> checkCodeExists(@PathVariable String code) {
        log.debug("Checking if route code exists: {}", code);

        boolean exists = routeService.existsByCode(code);
        return ResponseEntity.ok(exists);
    }
}