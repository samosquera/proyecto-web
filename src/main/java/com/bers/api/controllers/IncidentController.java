package com.bers.api.controllers;

import com.bers.api.dtos.IncidentDtos.IncidentCreateRequest;
import com.bers.api.dtos.IncidentDtos.IncidentResponse;
import com.bers.api.dtos.IncidentDtos.IncidentUpdateRequest;
import com.bers.domain.entities.enums.EntityType;
import com.bers.domain.entities.enums.IncidentType;
import com.bers.services.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'DISPATCHER', 'ADMIN')")
public class IncidentController {

    private final IncidentService incidentService;

    // ==================== CRUD BÁSICO ====================

    @PostMapping("/create")
    public ResponseEntity<IncidentResponse> createIncident(@Valid @RequestBody IncidentCreateRequest request) {
        log.info("Creating incident - type: {}, entity: {} ({})",
                request.type(), request.entityType(), request.entityId());

        IncidentResponse created = incidentService.createIncident(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/all")
    public ResponseEntity<List<IncidentResponse>> getAllIncidents() {
        log.debug("Retrieving all incidents");

        List<IncidentResponse> incidents = incidentService.getAllIncidents();
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> getIncidentById(@PathVariable Long id) {
        log.debug("Retrieving incident: {}", id);

        IncidentResponse incident = incidentService.getIncidentById(id);
        return ResponseEntity.ok(incident);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<IncidentResponse> updateIncident(
            @PathVariable Long id,
            @Valid @RequestBody IncidentUpdateRequest request
    ) {
        log.info("Updating incident: {}", id);

        IncidentResponse updated = incidentService.updateIncident(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteIncident(@PathVariable Long id) {
        log.warn("Deleting incident: {}", id);

        incidentService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== CONSULTAS ====================

    @GetMapping("/incident-entity")
    public ResponseEntity<List<IncidentResponse>> getIncidentsByEntity(
            @RequestParam EntityType entityType,
            @RequestParam Long entityId
    ) {
        log.debug("Retrieving incidents for entity: {} ({})", entityType, entityId);

        List<IncidentResponse> incidents = incidentService.getIncidentsByEntityTypeAndId(entityType, entityId);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<IncidentResponse>> getIncidentsByType(@PathVariable IncidentType type) {
        log.debug("Retrieving incidents of type: {}", type);

        List<IncidentResponse> incidents = incidentService.getIncidentsByType(type);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/reported-by/{reportedById}")
    public ResponseEntity<List<IncidentResponse>> getIncidentsByReportedBy(@PathVariable Long reportedById) {
        log.debug("Retrieving incidents reported by user: {}", reportedById);

        List<IncidentResponse> incidents = incidentService.getIncidentsByReportedBy(reportedById);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<IncidentResponse>> getIncidentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        log.debug("Retrieving incidents between {} and {}", start, end);

        List<IncidentResponse> incidents = incidentService.getIncidentsByDateRange(start, end);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/type/{type}/count")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<Long> countIncidentsByType(
            @PathVariable IncidentType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since
    ) {
        log.debug("Counting incidents of type {} since {}", type, since);

        long count = incidentService.countIncidentsByType(type, since);
        return ResponseEntity.ok(count);
    }
}