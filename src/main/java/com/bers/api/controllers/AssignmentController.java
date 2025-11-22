package com.bers.api.controllers;

import com.bers.api.dtos.AssignmentDtos.AssignmentCreateRequest;
import com.bers.api.dtos.AssignmentDtos.AssignmentResponse;
import com.bers.api.dtos.AssignmentDtos.AssignmentUpdateRequest;
import com.bers.security.config.CustomUserDetails;
import com.bers.services.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
public class AssignmentController {

    private final AssignmentService assignmentService;

    // ==================== CRUD BÁSICO ====================

    @PostMapping("/create")
    public ResponseEntity<AssignmentResponse> createAssignment(
            @Valid @RequestBody AssignmentCreateRequest request,
            Authentication authentication) {
        log.info("Creating assignment for trip {} - driver: {}",

                request.tripId(), request.driverId());

        // Si no se proporciona dispatcherId, obtenerlo del token
        Long dispatcherId = request.dispatcherId();
        if (dispatcherId == null) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            dispatcherId = userDetails.getId();
            log.debug("DispatcherId obtained from token: {}", dispatcherId);
        }

        // Crear nuevo request con el dispatcherId correcto

        AssignmentCreateRequest requestWithDispatcher = new AssignmentCreateRequest(
                request.tripId(), request.driverId(), dispatcherId);

        AssignmentResponse created = assignmentService.createAssignment(requestWithDispatcher);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/all")
    public ResponseEntity<List<AssignmentResponse>> getAllAssignments() {
        log.debug("Retrieving all assignments");

        List<AssignmentResponse> assignments = assignmentService.getAllAssignments();
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponse> getAssignmentById(@PathVariable Long id) {
        log.debug("Retrieving assignment: {}", id);

        AssignmentResponse assignment = assignmentService.getAssignmentById(id);
        return ResponseEntity.ok(assignment);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<AssignmentResponse> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentUpdateRequest request
    ) {
        log.info("Updating assignment: {}", id);

        AssignmentResponse updated = assignmentService.updateAssignment(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        log.warn("Deleting assignment: {}", id);

        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== CONSULTAS ====================

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<AssignmentResponse> getAssignmentByTrip(@PathVariable Long tripId) {
        log.debug("Retrieving assignment for trip: {}", tripId);

        AssignmentResponse assignment = assignmentService.getAssignmentByTripId(tripId);
        return ResponseEntity.ok(assignment);
    }

    @GetMapping("/trip/{tripId}/details")
    public ResponseEntity<AssignmentResponse> getAssignmentWithDetails(@PathVariable Long tripId) {
        log.debug("Retrieving assignment with details for trip: {}", tripId);

        AssignmentResponse assignment = assignmentService.getAssignmentWithDetails(tripId);
        return ResponseEntity.ok(assignment);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsByDriver(@PathVariable Long driverId) {
        log.debug("Retrieving assignments for driver: {}", driverId);

        List<AssignmentResponse> assignments = assignmentService.getAssignmentsByDriverId(driverId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/driver/{driverId}/active")
    public ResponseEntity<List<AssignmentResponse>> getActiveAssignmentsByDriver(
            @PathVariable Long driverId
    ) {
        log.debug("Retrieving active assignments for driver: {}", driverId);

        List<AssignmentResponse> assignments = assignmentService.getActiveAssignmentsByDriver(driverId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/driver/{driverId}/date")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsByDriverAndDate(
            @PathVariable Long driverId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.debug("Retrieving assignments for driver {} on date: {}", driverId, date);

        List<AssignmentResponse> assignments = assignmentService.getAssignmentsByDriverAndDate(driverId, date);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/dispatcher/{dispatcherId}")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsByDispatcher(
            @PathVariable Long dispatcherId
    ) {
        log.debug("Retrieving assignments created by dispatcher: {}", dispatcherId);

        List<AssignmentResponse> assignments = assignmentService.getAssignmentsByDispatcherId(dispatcherId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        log.debug("Retrieving assignments between {} and {}", start, end);

        List<AssignmentResponse> assignments = assignmentService.getAssignmentsByDateRange(start, end);
        return ResponseEntity.ok(assignments);
    }

    // ==================== ACCIONES ====================

    @PostMapping("/{id}/approve-checklist")
    public ResponseEntity<AssignmentResponse> approveChecklist(@PathVariable Long id) {
        log.info("Approving checklist for assignment: {}", id);

        AssignmentResponse updated = assignmentService.approveChecklist(id);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/trip/{tripId}/has-assignment")
    public ResponseEntity<Boolean> hasActiveAssignment(@PathVariable Long tripId) {
        log.debug("Checking if trip {} has active assignment", tripId);

        boolean hasAssignment = assignmentService.hasActiveAssignment(tripId);
        return ResponseEntity.ok(hasAssignment);
    }
}