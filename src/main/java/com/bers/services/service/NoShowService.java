package com.bers.services.service;

import com.bers.domain.entities.Ticket;

import java.math.BigDecimal;
import java.util.List;

public interface NoShowService {
    void processNoShowTickets(Long tripId);

    void processUpcomingTripsNoShow();

    void releaseNoShowSeat(Long ticketId);

    BigDecimal calculateNoShowFee(Ticket t);

    List<String> getAvailableSeatsFromNoShow(Long tripId);
}