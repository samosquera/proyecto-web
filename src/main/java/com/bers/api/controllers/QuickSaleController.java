package com.bers.api.controllers;

import com.bers.api.dtos.QuickSaleDtos.AvailableQuickSaleSeatsResponse;
import com.bers.api.dtos.QuickSaleDtos.QuickSaleRequest;
import com.bers.api.dtos.QuickSaleDtos.QuickSaleResponse;
import com.bers.services.service.QuickSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quick-sale")
@RequiredArgsConstructor
@Slf4j
public class QuickSaleController {

    private final QuickSaleService quickSaleService;

    /**
     * Realizar venta rápida de último minuto
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    public ResponseEntity<QuickSaleResponse> createQuickSale(
            @Valid @RequestBody QuickSaleRequest request
    ) {
        log.info("Creating quick sale for trip {} seat {}", request.tripId(), request.seatNumber());
        QuickSaleResponse response = quickSaleService.createQuickSale(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener asientos disponibles para venta rápida
     */
    @GetMapping("/available/{tripId}")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<AvailableQuickSaleSeatsResponse> getAvailableSeats(
            @PathVariable Long tripId
    ) {
        log.debug("Getting available quick sale seats for trip {}", tripId);
        AvailableQuickSaleSeatsResponse response = quickSaleService.getAvailableQuickSaleSeats(tripId);
        return ResponseEntity.ok(response);
    }
}
