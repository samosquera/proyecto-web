package com.bers.api.controllers;

import com.bers.api.dtos.OverbookingDtos.OverbookingApproveRequest;
import com.bers.api.dtos.OverbookingDtos.OverbookingCreateRequest;
import com.bers.api.dtos.OverbookingDtos.OverbookingRejectRequest;
import com.bers.api.dtos.OverbookingDtos.OverbookingResponse;
import com.bers.security.config.CustomUserDetails;
import com.bers.services.service.OverbookingService;
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
@RequestMapping("/api/v1/overbooking")
@RequiredArgsConstructor
@Slf4j
public class OverbookingController {

    private final OverbookingService overbookingService;

    /**
     * SOLICITAR OVERBOOKING - CLERK y ADMIN
     */
    @PostMapping("/request")
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    public ResponseEntity<OverbookingResponse> requestOverbooking(
            Authentication authentication,
            @Valid @RequestBody OverbookingCreateRequest request
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        log.info("User {} requesting overbooking for trip: {}", userId, request.tripId());

        OverbookingResponse response = overbookingService.requestOverbooking(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * APROBAR OVERBOOKING - SOLO DISPATCHER
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<OverbookingResponse> approveOverbooking(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody OverbookingApproveRequest request
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long dispatcherId = userDetails.getId();

        log.info("Dispatcher {} approving overbooking request: {}", dispatcherId, id);

        OverbookingResponse response = overbookingService.approveOverbooking(id, dispatcherId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * RECHAZAR OVERBOOKING - SOLO DISPATCHER
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<OverbookingResponse> rejectOverbooking(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody OverbookingRejectRequest request
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long dispatcherId = userDetails.getId();

        log.info("Dispatcher {} rejecting overbooking request: {}", dispatcherId, id);

        OverbookingResponse response = overbookingService.rejectOverbooking(id, dispatcherId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * OBTENER SOLICITUDES PENDIENTES - DISPATCHER y ADMIN
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<OverbookingResponse>> getPendingRequests() {
        log.debug("Retrieving pending overbooking requests");

        List<OverbookingResponse> requests = overbookingService.getPendingRequests();
        return ResponseEntity.ok(requests);
    }

    /**
     * OBTENER SOLICITUDES POR VIAJE - DISPATCHER y ADMIN
     */
    @GetMapping("/trip/{tripId}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<OverbookingResponse>> getOverbookingRequestsByTrip(
            @PathVariable Long tripId
    ) {
        log.debug("Retrieving overbooking requests for trip: {}", tripId);

        List<OverbookingResponse> requests = overbookingService.getOverbookingRequestsByTrip(tripId);
        return ResponseEntity.ok(requests);
    }

    /**
     * OBTENER SOLICITUDES POR ESTADO - DISPATCHER y ADMIN
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<OverbookingResponse>> getOverbookingRequestsByStatus(
            @PathVariable String status
    ) {
        log.debug("Retrieving overbooking requests with status: {}", status);

        List<OverbookingResponse> requests = overbookingService.getOverbookingRequestsByStatus(status);
        return ResponseEntity.ok(requests);
    }

    /**
     * OBTENER SOLICITUD POR ID - DISPATCHER y ADMIN
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<OverbookingResponse> getOverbookingRequestById(
            @PathVariable Long id
    ) {
        log.debug("Retrieving overbooking request: {}", id);

        OverbookingResponse request = overbookingService.getOverbookingRequestById(id);
        return ResponseEntity.ok(request);
    }

    /**
     * VERIFICAR SI SE PUEDE HACER OVERBOOKING EN UN VIAJE - CLERK y ADMIN
     */
    @GetMapping("/trip/{tripId}/can-overbook")
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    public ResponseEntity<Boolean> canOverbook(
            @PathVariable Long tripId
    ) {
        log.debug("Checking if overbooking is allowed for trip: {}", tripId);

        boolean canOverbook = overbookingService.canOverbook(tripId);
        return ResponseEntity.ok(canOverbook);
    }

    /**
     * OBTENER TASA DE OCUPACIÓN ACTUAL - CLERK, DISPATCHER y ADMIN
     */
    @GetMapping("/trip/{tripId}/occupancy")
    @PreAuthorize("hasAnyRole('CLERK', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<Double> getCurrentOccupancyRate(
            @PathVariable Long tripId
    ) {
        log.debug("Getting current occupancy rate for trip: {}", tripId);

        double occupancyRate = overbookingService.getCurrentOccupancyRate(tripId);
        return ResponseEntity.ok(occupancyRate);
    }

    /**
     * EXPIRAR SOLICITUDES PENDIENTES - SOLO ADMIN (para jobs programados)
     */
    @PostMapping("/expire-pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> expirePendingRequests() {
        log.info("Expiring pending overbooking requests");

        overbookingService.expirePendingRequests();
        return ResponseEntity.ok().build();
    }
}