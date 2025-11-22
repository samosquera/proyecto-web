package com.bers.api.controllers;

import com.bers.api.dtos.TicketDtos.TicketCreateRequest;
import com.bers.api.dtos.TicketDtos.TicketPaymentConfirmRequest;
import com.bers.api.dtos.TicketDtos.TicketResponse;
import com.bers.api.dtos.TicketDtos.TicketUpdateRequest;
import com.bers.domain.entities.enums.TicketStatus;
import com.bers.security.config.CustomUserDetails;
import com.bers.services.service.TicketService;
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
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {

    private final TicketService ticketService;

    // Utilidades passenger/clerk

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('PASSENGER', 'CLERK', 'ADMIN')")
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody TicketCreateRequest request
    ) {
        log.info("Creating ticket for trip {} seat {}", request.tripId(), request.seatNumber());

        TicketResponse created = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{id}/confirm-payment")
    @PreAuthorize("hasAnyRole('PASSENGER', 'CLERK', 'ADMIN')")
    public ResponseEntity<TicketResponse> confirmPayment(
            @PathVariable Long id,
            @Valid @RequestBody TicketPaymentConfirmRequest request
    ) {
        log.info("Confirming payment for ticket {} with method {}", id, request.paymentMethod());

        TicketResponse confirmed = ticketService.confirmPayment(id, request);
        return ResponseEntity.ok(confirmed);
    }

    @GetMapping("/my-tickets")
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN')")
    public ResponseEntity<List<TicketResponse>> getMyTickets(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long passengerId = userDetails.getId();

        log.debug("User {} retrieving their tickets", passengerId);

        List<TicketResponse> tickets = ticketService.getTicketsByPassengerId(passengerId);
        return ResponseEntity.ok(tickets);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PASSENGER', 'CLERK', 'ADMIN')")
    public ResponseEntity<TicketResponse> cancelTicket(@PathVariable Long id) {
        log.info("Cancelling ticket: {}", id);

        TicketResponse cancelled = ticketService.cancelTicket(id);
        return ResponseEntity.ok(cancelled);
    }

    // ==================== CLERK/ADMIN ENDPOINTS ====================

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    public ResponseEntity<List<TicketResponse>> getAllTickets() {
        log.debug("Retrieving all tickets");

        List<TicketResponse> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        log.debug("Retrieving ticket: {}", id);

        TicketResponse ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyRole('PASSENGER','CLERK', 'DRIVER', 'ADMIN')")
    public ResponseEntity<TicketResponse> getTicketWithDetails(@PathVariable Long id) {
        log.debug("Retrieving ticket with details: {}", id);

        TicketResponse ticket = ticketService.getTicketWithDetails(id);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/qr/{qrCode}")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    public ResponseEntity<TicketResponse> getTicketByQrCode(@PathVariable String qrCode) {
        log.debug("Retrieving ticket by QR code: {}", qrCode);

        TicketResponse ticket = ticketService.getTicketByQrCode(qrCode);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/trip/{tripId}")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<TicketResponse>> getTicketsByTrip(@PathVariable Long tripId) {
        log.debug("Retrieving tickets for trip: {}", tripId);

        List<TicketResponse> tickets = ticketService.getTicketsByTripId(tripId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/trip/{tripId}/status/{status}")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<TicketResponse>> getTicketsByTripAndStatus(
            @PathVariable Long tripId,
            @PathVariable TicketStatus status
    ) {
        log.debug("Retrieving tickets for trip {} with status: {}", tripId, status);

        List<TicketResponse> tickets = ticketService.getTicketsByTripAndStatus(tripId, status);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/passenger/{passengerId}")
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    public ResponseEntity<List<TicketResponse>> getTicketsByPassenger(@PathVariable Long passengerId) {
        log.debug("Retrieving tickets for passenger: {}", passengerId);

        List<TicketResponse> tickets = ticketService.getTicketsByPassengerId(passengerId);
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketUpdateRequest request
    ) {
        log.info("Updating ticket: {}", id);

        TicketResponse updated = ticketService.updateTicket(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        log.warn("Deleting ticket: {}", id);

        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    // Cosas para los conductores bro

    //Boardind check

    @PostMapping("/{id}/used")
    @PreAuthorize("hasAnyRole('DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<TicketResponse> markAsUsed(@PathVariable Long id) {
        log.info("Marking ticket as used: {}", id);

        TicketResponse updated = ticketService.markAsUsed(id);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<TicketResponse> markAsNoShow(@PathVariable Long id) {
        log.info("Marking ticket as no-show: {}", id);

        TicketResponse updated = ticketService.markAsNoShow(id);
        return ResponseEntity.ok(updated);
    }

    // consultas utiles

    @GetMapping("/check-availability")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<Boolean> isSeatAvailable(
            @RequestParam Long tripId,
            @RequestParam String seatNumber
    ) {
        log.debug("Checking seat {} availability for trip: {}", seatNumber, tripId);

        boolean available = ticketService.isSeatAvailable(tripId, seatNumber);
        return ResponseEntity.ok(available);
    }

    @GetMapping("/trip/{tripId}/count")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<Long> countSoldTicketsByTrip(@PathVariable Long tripId) {
        log.debug("Counting sold tickets for trip: {}", tripId);

        long count = ticketService.countSoldTicketsByTrip(tripId);
        return ResponseEntity.ok(count);
    }
}