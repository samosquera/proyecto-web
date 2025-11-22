package com.bers.api.controllers;

import com.bers.api.dtos.BusDtos.BusCreateRequest;
import com.bers.api.dtos.BusDtos.BusResponse;
import com.bers.api.dtos.BusDtos.BusUpdateRequest;
import com.bers.domain.entities.enums.BusStatus;
import com.bers.services.service.BusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buses")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
public class BusController {

    private final BusService busService;

    // ==================== CRUD BÁSICO ====================

    @PostMapping("/create")
    public ResponseEntity<BusResponse> createBus(@Valid @RequestBody BusCreateRequest request) {
        log.info("Creating new bus with plate: {}", request.plate());

        BusResponse created = busService.createBus(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/all")
    public ResponseEntity<List<BusResponse>> getAllBuses() {
        log.debug("Retrieving all buses");

        List<BusResponse> buses = busService.getAllBuses();
        return ResponseEntity.ok(buses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusResponse> getBusById(@PathVariable Long id) {
        log.debug("Retrieving bus: {}", id);

        BusResponse bus = busService.getBusById(id);
        return ResponseEntity.ok(bus);
    }

    @GetMapping("/{id}/with-seats")
    public ResponseEntity<BusResponse> getBusWithSeats(@PathVariable Long id) {
        log.debug("Retrieving bus with seats: {}", id);

        BusResponse bus = busService.getBusWithSeats(id);
        return ResponseEntity.ok(bus);
    }

    @GetMapping("/plate/{plate}")
    public ResponseEntity<BusResponse> getBusByPlate(@PathVariable String plate) {
        log.debug("Retrieving bus by plate: {}", plate);

        BusResponse bus = busService.getBusbyPlate(plate);
        return ResponseEntity.ok(bus);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<BusResponse> updateBus(
            @PathVariable Long id,
            @Valid @RequestBody BusUpdateRequest request
    ) {
        log.info("Updating bus: {}", id);

        BusResponse updated = busService.updateBus(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBus(@PathVariable Long id) {
        log.warn("Deleting bus: {}", id);

        busService.deleteBus(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== FILTROS Y CONSULTAS ====================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BusResponse>> getBusesByStatus(@PathVariable BusStatus status) {
        log.debug("Retrieving buses with status: {}", status);

        List<BusResponse> buses = busService.getBusesByStatus(status);
        return ResponseEntity.ok(buses);
    }

    @GetMapping("/available")
    public ResponseEntity<List<BusResponse>> getAvailableBuses(
            @RequestParam(required = false, defaultValue = "1") Integer minCapacity
    ) {
        log.debug("Retrieving available buses with min capacity: {}", minCapacity);

        List<BusResponse> buses = busService.getAvailableBuses(minCapacity);
        return ResponseEntity.ok(buses);
    }

    @GetMapping("/plate/{plate}/exists")
    public ResponseEntity<Boolean> checkPlateExists(@PathVariable String plate) {
        log.debug("Checking if plate exists: {}", plate);

        boolean exists = busService.existsByPlate(plate);
        return ResponseEntity.ok(exists);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BusResponse> changeBusStatus(
            @PathVariable Long id,
            @RequestParam BusStatus status
    ) {
        log.info("Changing status of bus {} to {}", id, status);

        BusResponse updated = busService.changeBusStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}