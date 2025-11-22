package com.bers.api.controllers;

import com.bers.api.dtos.StopDtos.StopCreateRequest;
import com.bers.api.dtos.StopDtos.StopResponse;
import com.bers.api.dtos.StopDtos.StopUpdateRequest;
import com.bers.services.service.StopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stops")
@RequiredArgsConstructor
@Slf4j
public class StopController {

    private final StopService stopService;

    //Public

    @GetMapping("all")
    public ResponseEntity<List<StopResponse>> getAllStops() {
        log.debug("Retrieving all stops");

        List<StopResponse> stops = stopService.getAllStops();
        return ResponseEntity.ok(stops);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StopResponse> getStopById(@PathVariable Long id) {
        log.debug("Retrieving stop: {}", id);

        StopResponse stop = stopService.getStopById(id);
        return ResponseEntity.ok(stop);
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<StopResponse>> getStopsByRoute(@PathVariable Long routeId) {
        log.debug("Retrieving stops for route: {}", routeId);

        List<StopResponse> stops = stopService.getStopsByRouteId(routeId);
        return ResponseEntity.ok(stops);
    }

    @GetMapping("/search")
    public ResponseEntity<List<StopResponse>> searchStopsByName(@RequestParam String name) {
        log.debug("Searching stops by name: {}", name);

        List<StopResponse> stops = stopService.searchStopsByName(name);
        return ResponseEntity.ok(stops);
    }

    // Listar posibles destinos (paradas con order mayor a fromStopId) xd
    @GetMapping("/available-destinations/{fromStopId}")
    public ResponseEntity<List<StopResponse>> getAvailableDestinations(@PathVariable Long fromStopId) {
        List<StopResponse> response = stopService.getAvailableDestinations(fromStopId);
        return ResponseEntity.ok(response);
    }

    // Dispatcher/Admin

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<StopResponse> createStop(@Valid @RequestBody StopCreateRequest request) {
        log.info("Creating new stop for route: {}", request.routeId());

        StopResponse created = stopService.createStop(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/batch-create")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<StopResponse>> createStopsBatch(
            @Valid @RequestBody List<StopCreateRequest> requests) {

        log.info("Creating batch of {} stops for route {}", requests.size(), requests.getFirst().routeId());

        List<StopResponse> createdStops = requests.stream()
                .map(stopService::createStop)  // reutiliza el mismo servicio
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(createdStops);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<StopResponse> updateStop(
            @PathVariable Long id,
            @Valid @RequestBody StopUpdateRequest request
    ) {
        log.info("Updating stop: {}", id);

        StopResponse updated = stopService.updateStop(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStop(@PathVariable Long id) {
        log.warn("Deleting stop: {}", id);

        stopService.deleteStop(id);
        return ResponseEntity.noContent().build();
    }
}