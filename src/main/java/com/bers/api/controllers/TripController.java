package com.bers.api.controllers;

import com.bers.api.dtos.AssignmentDtos.AssignmentCreateRequest;
import com.bers.api.dtos.AssignmentDtos.AssignmentResponse;
import com.bers.api.dtos.SeatHoldDtos.SeatHoldCreateRequest;
import com.bers.api.dtos.SeatHoldDtos.SeatHoldResponse;
import com.bers.api.dtos.TicketDtos.TicketCreateRequest;
import com.bers.api.dtos.TicketDtos.TicketResponse;
import com.bers.api.dtos.TripDtos.TripCreateRequest;
import com.bers.api.dtos.TripDtos.TripResponse;
import com.bers.api.dtos.TripDtos.TripUpdateRequest;
import com.bers.domain.entities.enums.TripStatus;
import com.bers.security.config.CustomUserDetails;
import com.bers.services.service.AssignmentService;
import com.bers.services.service.SeatHoldService;
import com.bers.services.service.TicketService;
import com.bers.services.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Validated
@Slf4j
public class TripController {

    private final TripService tripService;
    private final AssignmentService assignmentService;
    private final SeatHoldService seatHoldService;
    private final TicketService ticketService;


    @PostMapping("create")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TripResponse> createTrip(@Valid @RequestBody TripCreateRequest request) {
        log.info("Creating new trip for route: {}, date: {}", request.routeId(), request.date());
        TripResponse response = tripService.createTrip(request);
        log.info("Trip created successfully with ID: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<TripResponse>> filterTrips(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<TripResponse> trips = tripService.filterTripByOriginAndDestination(origin, destination, date);
        return ResponseEntity.ok(trips);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<TripResponse> updateTrip(
            @PathVariable Long id,
            @Valid @RequestBody TripUpdateRequest request) {
        log.info("Updating trip ID: {}", id);
        TripResponse response = tripService.updateTrip(id, request);
        log.info("Trip ID: {} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/{id}")
    public ResponseEntity<TripResponse> getTripById(@PathVariable Long id) {
        log.debug("Getting trip by ID: {}", id);
        TripResponse response = tripService.getTripById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<TripResponse> getTripWithDetails(@PathVariable Long id) {
        log.debug("Getting trip details for ID: {}", id);
        TripResponse response = tripService.getTripWithDetails(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<TripResponse>> getAllTrips() {
        log.debug("Getting all trips");
        List<TripResponse> response = tripService.getAllTrips();
        return ResponseEntity.ok(response);
    }


    @GetMapping("/search")
    public ResponseEntity<List<TripResponse>> searchTrips(
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) TripStatus status) {
        log.debug("Searching trips - routeId: {}, date: {}, status {}", routeId, date, status);
        List<TripResponse> response = tripService.searchTrips(routeId, date, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<TripResponse>> getTripsByRouteAndDate(
            @PathVariable Long routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.debug("Getting trips for route: {} and date: {}", routeId, date);
        List<TripResponse> response = tripService.getTripsByRouteAndDate(routeId, date);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bus/{busId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'DRIVER')")
    public ResponseEntity<List<TripResponse>> getActiveTripsByBus(
            @PathVariable Long busId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.debug("Getting active trips for bus: {} on date: {}", busId, date);
        List<TripResponse> response = tripService.getActiveTripsByBus(busId, date);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'DRIVER')")
    public ResponseEntity<TripResponse> changeTripStatus(
            @PathVariable Long id,
            @RequestParam TripStatus status) {
        log.info("Changing trip ID: {} status to: {}", id, status);
        TripResponse response = tripService.changeTripStatus(id, status);
        log.info("Trip ID: {} status changed to: {}", id, status);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/boarding/open")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<TripResponse> openBoarding(@PathVariable Long id) {
        log.info("Opening boarding for trip ID: {}", id);
        TripResponse response = tripService.changeTripStatus(id, TripStatus.BOARDING);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/boarding/close")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<TripResponse> closeBoarding(@PathVariable Long id) {
        log.info("Closing boarding for trip ID: {}", id);
        TripResponse response = tripService.changeTripStatus(id, TripStatus.DEPARTED);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/depart")
    @PreAuthorize("hasAnyRole('DRIVER', 'DISPATCHER')")
    public ResponseEntity<TripResponse> markAsDeparted(@PathVariable Long id) {
        log.info("Marking trip ID: {} as departed", id);
        TripResponse response = tripService.changeTripStatus(id, TripStatus.DEPARTED);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/arrive")
    @PreAuthorize("hasAnyRole('DRIVER', 'DISPATCHER')")
    public ResponseEntity<TripResponse> markAsArrived(@PathVariable Long id) {
        log.info("Marking trip ID: {} as arrived", id);
        TripResponse response = tripService.changeTripStatus(id, TripStatus.ARRIVED);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<TripResponse> cancelTrip(@PathVariable Long id) {
        log.info("Canceling trip ID: {}", id);
        TripResponse response = tripService.changeTripStatus(id, TripStatus.CANCELLED);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<TripResponse> reactivateTrip(@PathVariable Long id) {
        log.info("Reactivating cancelled trip ID: {}", id);
        TripResponse response = tripService.changeTripStatus(id, TripStatus.SCHEDULED);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id) {
        log.info("Deleting trip ID: {}", id);
        tripService.deleteTrip(id);
        log.info("Trip ID: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/seats/{seat}/hold")
    @PreAuthorize("hasAnyRole('PASSENGER', 'CLERK', 'ADMIN')")
    public ResponseEntity<SeatHoldResponse> holdSeat(
            @PathVariable Long id,
            @PathVariable String seat,
            @RequestParam Long fromStopId,
            @RequestParam Long toStopId,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        log.info("User {} holding seat {} for trip {}", userId, seat, id);

        SeatHoldCreateRequest request = new SeatHoldCreateRequest(id, seat, fromStopId, toStopId);
        SeatHoldResponse response = seatHoldService.createSeatHold(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/tickets")
    @PreAuthorize("hasAnyRole('PASSENGER', 'CLERK', 'ADMIN')")
    public ResponseEntity<TicketResponse> purchaseTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketCreateRequest request) {

        // Validar que el tripId del request coincida con el path
        if (!id.equals(request.tripId())) {
            throw new IllegalArgumentException("Trip ID mismatch between path and request body");
        }

        log.info("Purchasing ticket for trip {} seat {}", id, request.seatNumber());

        TicketResponse response = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<AssignmentResponse> assignTripResources(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentCreateRequest request,
            Authentication authentication) {

        // Validar que el tripId del request coincida con el path
        if (!id.equals(request.tripId())) {
            throw new IllegalArgumentException("Trip ID mismatch between path and request body");
        }

        log.info("Dispatcher assigning trip {} to driver {}", id, request.driverId());

        AssignmentResponse response = assignmentService.createAssignment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/today")
    public ResponseEntity<List<TripResponse>> getTodayTrips() {
        log.debug("Getting today's trips");
        List<TripResponse> response = tripService.getTripsByDate(LocalDate.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<TripResponse>> getTripsByStatus(@PathVariable TripStatus status) {
        log.debug("Getting trips by status: {}", status);
        List<TripResponse> response = tripService.getTripsByStatus(status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today/active")
    public ResponseEntity<List<TripResponse>> getTodayActiveTrips() {
        log.debug("Getting today's active trips");
        List<TripResponse> response = tripService.getTodayActiveTrips();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/driver/my-trips")
    @PreAuthorize("hasAnyRole('DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<AssignmentResponse>> getDriverTrips(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {

        log.debug("Getting driver trips for date: {}", date);

        try {
            // Obtener el ID del conductor autenticado
            Long driverId = getCurrentDriverId(authentication);

            List<AssignmentResponse> assignments = assignmentService.getAssignmentsByDriverAndDate(driverId, date);

            log.debug("Found {} assignments for driver {} on date {}",
                    assignments.size(), driverId, date);
            return ResponseEntity.ok(assignments);

        } catch (IllegalArgumentException e) {
            log.warn("Error getting driver trips: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error getting driver trips", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/driver/current-trips")
    @PreAuthorize("hasAnyRole('DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<AssignmentResponse>> getCurrentDriverTrips(Authentication authentication) {
        log.debug("Getting current driver trips");

        try {
            Long driverId = getCurrentDriverId(authentication);
            LocalDate today = LocalDate.now();

            List<AssignmentResponse> assignments = assignmentService.getAssignmentsByDriverAndDate(driverId, today);

            log.debug("Found {} current assignments for driver {}", assignments.size(), driverId);
            return ResponseEntity.ok(assignments);

        } catch (IllegalArgumentException e) {
            log.warn("Error getting current driver trips: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error getting current driver trips", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/driver/active-trips")
    @PreAuthorize("hasAnyRole('DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<AssignmentResponse>> getActiveDriverTrips(Authentication authentication) {
        log.debug("Getting active driver trips");

        try {
            Long driverId = getCurrentDriverId(authentication);

            List<AssignmentResponse> assignments = assignmentService.getActiveAssignmentsByDriver(driverId);

            log.debug("Found {} active assignments for driver {}", assignments.size(), driverId);
            return ResponseEntity.ok(assignments);

        } catch (IllegalArgumentException e) {
            log.warn("Error getting active driver trips: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error getting active driver trips", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    //Metodo auxiliar para obtener el ID del conductor autenticado

    private Long getCurrentDriverId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("User not authenticated");
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            // Verificar que el usuario sea un conductor
            if (!userDetails.hasRole("DRIVER")) {
                throw new IllegalArgumentException("User is not a driver");
            }
            return userDetails.getId();
        }

        throw new IllegalArgumentException("Unable to get driver ID from authentication");
    }
}