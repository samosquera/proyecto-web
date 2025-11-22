package com.bers.services.service;

import com.bers.api.dtos.TicketDtos.TicketCreateRequest;
import com.bers.api.dtos.TicketDtos.TicketPaymentConfirmRequest;
import com.bers.api.dtos.TicketDtos.TicketResponse;
import com.bers.api.dtos.TicketDtos.TicketUpdateRequest;
import com.bers.domain.entities.enums.TicketStatus;

import java.util.List;

public interface TicketService {
    TicketResponse createTicket(TicketCreateRequest request);

    TicketResponse confirmPayment(Long ticketId, TicketPaymentConfirmRequest request);

    TicketResponse updateTicket(Long id, TicketUpdateRequest request);

    TicketResponse getTicketById(Long id);

    TicketResponse getTicketByQrCode(String qrCode);

    TicketResponse getTicketWithDetails(Long id);

    List<TicketResponse> getAllTickets();

    List<TicketResponse> getTicketsByTripId(Long tripId);

    List<TicketResponse> getTicketsByPassengerId(Long passengerId);

    List<TicketResponse> getTicketsByTripAndStatus(Long tripId, TicketStatus status);

    void deleteTicket(Long id);

    TicketResponse cancelTicket(Long id);

    TicketResponse markAsNoShow(Long id);

    TicketResponse markAsUsed(Long id);

    boolean isSeatAvailable(Long tripId, String seatNumber);

    long countSoldTicketsByTrip(Long tripId);
}
