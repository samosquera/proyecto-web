package com.bers.api.controllers;

import com.bers.api.dtos.FareRuleDtos.FareRuleCreateRequest;
import com.bers.api.dtos.FareRuleDtos.FareRuleResponse;
import com.bers.api.dtos.FareRuleDtos.FareRuleUpdateRequest;
import com.bers.services.service.FareRuleService;
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
@RequestMapping("/api/v1/fare-rules")
@RequiredArgsConstructor
@Slf4j
public class FareRuleController {

    private final FareRuleService fareRuleService;

    // ==================== ENDPOINTS PÚBLICOS ====================

    @GetMapping("/all")
    public ResponseEntity<List<FareRuleResponse>> getAllFareRules() {
        log.debug("Retrieving all fare rules");

        List<FareRuleResponse> fareRules = fareRuleService.getAllFareRules();
        return ResponseEntity.ok(fareRules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FareRuleResponse> getFareRuleById(@PathVariable Long id) {
        log.debug("Retrieving fare rule: {}", id);

        FareRuleResponse fareRule = fareRuleService.getFareRuleById(id);
        return ResponseEntity.ok(fareRule);
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<FareRuleResponse>> getFareRulesByRoute(@PathVariable Long routeId) {
        log.debug("Retrieving fare rules for route: {}", routeId);

        List<FareRuleResponse> fareRules = fareRuleService.getFareRulesByRouteId(routeId);
        return ResponseEntity.ok(fareRules);
    }

    @GetMapping("/segment")
    public ResponseEntity<FareRuleResponse> getFareRuleForSegment(
            @RequestParam Long routeId,
            @RequestParam Long fromStopId,
            @RequestParam Long toStopId
    ) {
        log.debug("Retrieving fare rule for segment: route={}, from={}, to={}",
                routeId, fromStopId, toStopId);

        FareRuleResponse fareRule = fareRuleService.getFareRuleForSegment(routeId, fromStopId, toStopId);
        return ResponseEntity.ok(fareRule);
    }

    @GetMapping("/route/{routeId}/dynamic")
    public ResponseEntity<List<FareRuleResponse>> getDynamicPricingRules(@PathVariable Long routeId) {
        log.debug("Retrieving dynamic pricing rules for route: {}", routeId);

        List<FareRuleResponse> fareRules = fareRuleService.getDynamicPricingRules(routeId);
        return ResponseEntity.ok(fareRules);
    }

    @GetMapping("/{id}/calculate-dynamic-price")
    public ResponseEntity<BigDecimal> calculateDynamicPrice(
            @PathVariable Long id,
            @RequestParam Double occupancyRate
    ) {
        log.debug("Calculating dynamic price for fare rule {} with occupancy: {}%",
                id, occupancyRate * 100);

        BigDecimal price = fareRuleService.calculateDynamicPrice(id, occupancyRate);
        return ResponseEntity.ok(price);
    }

    // ==================== DISPATCHER/ADMIN ENDPOINTS ====================

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<FareRuleResponse> createFareRule(@Valid @RequestBody FareRuleCreateRequest request) {
        log.info("Creating fare rule for route: {}", request.routeId());

        FareRuleResponse created = fareRuleService.createFareRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<FareRuleResponse> updateFareRule(
            @PathVariable Long id,
            @Valid @RequestBody FareRuleUpdateRequest request
    ) {
        log.info("Updating fare rule: {}", id);

        FareRuleResponse updated = fareRuleService.updateFareRule(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delte/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFareRule(@PathVariable Long id) {
        log.warn("Deleting fare rule: {}", id);

        fareRuleService.deleteFareRule(id);
        return ResponseEntity.noContent().build();
    }
}