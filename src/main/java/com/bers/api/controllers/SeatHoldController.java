package com.bers.api.controllers;

import com.bers.api.dtos.SeatHoldDtos.SeatHoldCreateRequest;
import com.bers.api.dtos.SeatHoldDtos.SeatHoldResponse;
import com.bers.api.dtos.SeatHoldDtos.SeatHoldUpdateRequest;
import com.bers.security.config.CustomUserDetails;
import com.bers.services.service.SeatHoldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seat-holds")
@RequiredArgsConstructor
@Slf4j
public class SeatHoldController {

    private final SeatHoldService seatHoldService;

    // ==================== ENDPOINTS AUTENTICADOS ====================

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('PASSENGER', 'CLERK', 'ADMIN')")
    public ResponseEntity<SeatHoldResponse> createSeatHold(
            Authentication authentication,
            @Valid @RequestBody SeatHoldCreateRequest request
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        log.info("User {} creating seat hold for trip {} seat {}",
                userId, request.tripId(), request.seatNumber());

        SeatHoldResponse created = seatHoldService.createSeatHold(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/my-holds")
    @PreAuthorize("hasAnyRole('PASSENGER', 'CLERK', 'ADMIN')")
    public ResponseEntity<List<SeatHoldResponse>> getMyHolds(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        log.debug("User {} retrieving their seat holds", userId);

        List<SeatHoldResponse> holds = seatHoldService.getSeatHoldsByUserId(userId);
        return ResponseEntity.ok(holds);
    }

    @DeleteMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('PASSENGER', 'CLERK', 'ADMIN')")
    public ResponseEntity<Void> releaseSeatHold(@PathVariable Long id) {
        log.info("Releasing seat hold: {}", id);

        seatHoldService.releaseSeatHold(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== CLERK/ADMIN ENDPOINTS ====================

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    public ResponseEntity<List<SeatHoldResponse>> getAllSeatHolds() {
        log.debug("Retrieving all seat holds");

        List<SeatHoldResponse> holds = seatHoldService.getAllSeatHolds();
        return ResponseEntity.ok(holds);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    public ResponseEntity<SeatHoldResponse> getSeatHoldById(@PathVariable Long id) {
        log.debug("Retrieving seat hold: {}", id);

        SeatHoldResponse hold = seatHoldService.getSeatHoldById(id);
        return ResponseEntity.ok(hold);
    }

    @GetMapping("/trip/{tripId}")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<SeatHoldResponse>> getSeatHoldsByTrip(@PathVariable Long tripId) {
        log.debug("Retrieving seat holds for trip: {}", tripId);

        List<SeatHoldResponse> holds = seatHoldService.getSeatHoldsByTripId(tripId);
        return ResponseEntity.ok(holds);
    }

    @GetMapping("/trip/{tripId}/active")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<SeatHoldResponse>> getActiveSeatHoldsByTrip(@PathVariable Long tripId) {
        log.debug("Retrieving active seat holds for trip: {}", tripId);

        List<SeatHoldResponse> holds = seatHoldService.getActiveSeatHoldsByTrip(tripId);
        return ResponseEntity.ok(holds);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    public ResponseEntity<List<SeatHoldResponse>> getSeatHoldsByUser(@PathVariable Long userId) {
        log.debug("Retrieving seat holds for user: {}", userId);

        List<SeatHoldResponse> holds = seatHoldService.getSeatHoldsByUserId(userId);
        return ResponseEntity.ok(holds);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    public ResponseEntity<SeatHoldResponse> updateSeatHold(
            @PathVariable Long id,
            @Valid @RequestBody SeatHoldUpdateRequest request
    ) {
        log.info("Updating seat hold: {}", id);

        SeatHoldResponse updated = seatHoldService.updateSeatHold(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    public ResponseEntity<Void> deleteSeatHold(@PathVariable Long id) {
        log.warn("Deleting seat hold: {}", id);

        seatHoldService.deleteSeatHold(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/convert")
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    public ResponseEntity<Void> convertHoldToTicket(@PathVariable Long id) {
        log.info("Converting seat hold {} to ticket", id);

        seatHoldService.convertHoldToTicket(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<Boolean> isSeatHeld(
            @RequestParam Long tripId,
            @RequestParam String seatNumber
    ) {
        log.debug("Checking if seat {} is held for trip: {}", seatNumber, tripId);

        boolean isHeld = seatHoldService.isSeatHeld(tripId, seatNumber);
        return ResponseEntity.ok(isHeld);
    }
}