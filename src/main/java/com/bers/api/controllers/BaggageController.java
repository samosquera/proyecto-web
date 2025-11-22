package com.bers.api.controllers;

import com.bers.api.dtos.BaggageDtos.BaggageCreateRequest;
import com.bers.api.dtos.BaggageDtos.BaggageResponse;
import com.bers.api.dtos.BaggageDtos.BaggageUpdateRequest;
import com.bers.services.service.BaggageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/baggage")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
public class BaggageController {

    private final BaggageService baggageService;

    // ==================== CRUD BÁSICO ====================

    @PostMapping("/create")
    public ResponseEntity<BaggageResponse> createBaggage(@Valid @RequestBody BaggageCreateRequest request) {
        log.info("Creating baggage for ticket: {}", request.ticketId());

        BaggageResponse created = baggageService.createBaggage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/all")
    public ResponseEntity<List<BaggageResponse>> getAllBaggage() {
        log.debug("Retrieving all baggage");

        List<BaggageResponse> baggage = baggageService.getAllBaggage();
        return ResponseEntity.ok(baggage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaggageResponse> getBaggageById(@PathVariable Long id) {
        log.debug("Retrieving baggage: {}", id);

        BaggageResponse baggage = baggageService.getBaggageById(id);
        return ResponseEntity.ok(baggage);
    }

    @GetMapping("/tag/{tagCode}")
    public ResponseEntity<BaggageResponse> getBaggageByTagCode(@PathVariable String tagCode) {
        log.debug("Retrieving baggage by tag: {}", tagCode);

        BaggageResponse baggage = baggageService.getBaggageByTagCode(tagCode);
        return ResponseEntity.ok(baggage);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<BaggageResponse> updateBaggage(
            @PathVariable Long id,
            @Valid @RequestBody BaggageUpdateRequest request
    ) {
        log.info("Updating baggage: {}", id);

        BaggageResponse updated = baggageService.updateBaggage(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBaggage(@PathVariable Long id) {
        log.warn("Deleting baggage: {}", id);

        baggageService.deleteBaggage(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== CONSULTAS ====================

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<BaggageResponse>> getBaggageByTicket(@PathVariable Long ticketId) {
        log.debug("Retrieving baggage for ticket: {}", ticketId);

        List<BaggageResponse> baggage = baggageService.getBaggageByTicketId(ticketId);
        return ResponseEntity.ok(baggage);
    }

    @GetMapping("/trip/{tripId}")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<BaggageResponse>> getBaggageByTrip(@PathVariable Long tripId) {
        log.debug("Retrieving baggage for trip: {}", tripId);

        List<BaggageResponse> baggage = baggageService.getBaggageByTripId(tripId);
        return ResponseEntity.ok(baggage);
    }

    @GetMapping("/trip/{tripId}/total-weight")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<BigDecimal> getTotalWeightByTrip(@PathVariable Long tripId) {
        log.debug("Calculating total baggage weight for trip: {}", tripId);

        BigDecimal totalWeight = baggageService.getTotalWeightByTrip(tripId);
        return ResponseEntity.ok(totalWeight);
    }

    @GetMapping("/calculate-fee")
    public ResponseEntity<BigDecimal> calculateBaggageFee(@RequestParam BigDecimal weightKg) {
        log.debug("Calculating baggage fee for weight: {} kg", weightKg);

        BigDecimal fee = baggageService.calculateBaggageFee(weightKg);
        return ResponseEntity.ok(fee);
    }
}