package com.bers.api.controllers;

import com.bers.api.dtos.SeatDtos;
import com.bers.api.dtos.SeatDtos.*;
import com.bers.domain.entities.enums.SeatType;
import com.bers.services.service.SeatService;
import com.bers.services.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seats")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
public class SeatController {

    private final SeatService seatService;
    private final TripService tripService;

    // ==================== CRUD BASICO ====================

    @PostMapping("/create")
    public ResponseEntity<SeatResponse> createSeat(@Valid @RequestBody SeatCreateRequest request) {
        log.info("Creating new seat {} for bus: {}", request.number(), request.busId());

        SeatResponse created = seatService.createSeat(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SeatResponse>> getAllSeats() {
        log.debug("Retrieving all seats");

        List<SeatResponse> seats = seatService.getAllSeats();
        return ResponseEntity.ok(seats);
    }

    @PostMapping("/batch-create")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<SeatDtos.SeatResponse>> createSeatsBatch(
            @Valid @RequestBody List<SeatDtos.SeatCreateRequest> requests) {

        log.info("Creating batch of {} seats for bus {}", requests.size(), requests.getFirst().busId());

        List<SeatDtos.SeatResponse> createdSeats = requests.stream()
                .map(seatService::createSeat)  // reutiliza el mismo servicio
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(createdSeats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatResponse> getSeatById(@PathVariable Long id) {
        log.debug("Retrieving seat: {}", id);

        SeatResponse seat = seatService.getSeatById(id);
        return ResponseEntity.ok(seat);
    }

    @GetMapping("/trips/{tripId}/seats")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SeatStatusBySegmentResponse> getSeatsBySegment(
            @PathVariable Long tripId,
            @RequestParam Long fromStopId,
            @RequestParam Long toStopId) {

        SeatStatusBySegmentResponse response =
                seatService.getTripSeatsForSegment(tripId, fromStopId, toStopId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<SeatResponse> updateSeat(
            @PathVariable Long id,
            @Valid @RequestBody SeatUpdateRequest request
    ) {
        log.info("Updating seat: {}", id);

        SeatResponse updated = seatService.updateSeat(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSeat(@PathVariable Long id) {
        log.warn("Deleting seat: {}", id);

        seatService.deleteSeat(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== CONSULTAS POR BUS ====================

    @GetMapping("/bus/{busId}")
    public ResponseEntity<List<SeatResponse>> getSeatsByBus(@PathVariable Long busId) {
        log.debug("Retrieving seats for bus: {}", busId);

        List<SeatResponse> seats = seatService.getSeatsByBusId(busId);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/{tripId}/full-seats-and-holds")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<SeatStatusResponse>> getSeatsWithHolds(@PathVariable Long tripId) {
        List<SeatStatusResponse> seats = seatService.getAllTripSeatsClassified(tripId);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/bus/{busId}/number/{number}")
    public ResponseEntity<SeatResponse> getSeatByBusAndNumber(
            @PathVariable Long busId,
            @PathVariable String number
    ) {
        log.debug("Retrieving seat {} for bus: {}", number, busId);

        SeatResponse seat = seatService.getSeatByBusAndNumber(busId, number);
        return ResponseEntity.ok(seat);
    }

    @GetMapping("/bus/{busId}/type/{type}")
    public ResponseEntity<List<SeatResponse>> getSeatsByBusAndType(
            @PathVariable Long busId,
            @PathVariable SeatType type
    ) {
        log.debug("Retrieving seats of type {} for bus: {}", type, busId);

        List<SeatResponse> seats = seatService.getSeatsByBusIdAndType(busId, type);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/bus/{busId}/count")
    public ResponseEntity<Long> countSeatsByBus(@PathVariable Long busId) {
        log.debug("Counting seats for bus: {}", busId);

        long count = seatService.countSeatsByBus(busId);
        return ResponseEntity.ok(count);
    }
}