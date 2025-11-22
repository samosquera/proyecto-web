package com.bers.services.service.serviceImple;

import com.bers.domain.entities.Ticket;
import com.bers.domain.entities.Trip;
import com.bers.domain.entities.enums.TicketStatus;
import com.bers.domain.entities.enums.TripStatus;
import com.bers.domain.repositories.TicketRepository;
import com.bers.domain.repositories.TripRepository;
import com.bers.services.event.SeatAvailableEvent;
import com.bers.services.service.ConfigService;
import com.bers.services.service.NoShowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NoShowServiceImpl implements NoShowService {

    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;
    private final ConfigService configService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void processNoShowTickets(Long tripId) {
        log.info("Processing no-show tickets for trip: {}", tripId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + tripId));

        // Verificar que el viaje está en BOARDING y próximo a salir
        if (trip.getStatus() != TripStatus.BOARDING) {
            log.debug("Trip {} is not in BOARDING status, skipping no-show processing", tripId);
            return;
        }

        // Obtener ventana de tiempo configurada (default 5 minutos)
        Integer noShowWindow = configService.getConfigValueAsInt("no.show.window.minutes", 5);
        LocalDateTime cutoffTime = trip.getDepartureAt().minusMinutes(noShowWindow);

        if (LocalDateTime.now().isBefore(cutoffTime)) {
            log.debug("Too early to process no-show for trip {}", tripId);
            return;
        }

        // Buscar tickets vendidos que no han sido usados
        List<Ticket> soldTickets = ticketRepository.findByTripIdAndStatus(tripId, TicketStatus.SOLD);

        for (Ticket ticket : soldTickets) {
            processNoShowTicket(ticket);
        }

        log.info("Processed {} no-show tickets for trip {}", soldTickets.size(), tripId);
    }

    private void processNoShowTicket(Ticket ticket) {
        // Solo procesar tickets en estado SOLD
        if (ticket.getStatus() != TicketStatus.SOLD) {
            log.debug("Ticket {} is not SOLD, skipping NO_SHOW processing", ticket.getId());
            return;
        }

        // Calcular el fee de NO_SHOW según precio del ticket y configuración
        BigDecimal noShowFee = calculateNoShowFee(ticket);

        // Marcar ticket como NO_SHOW
        ticket.setStatus(TicketStatus.NO_SHOW);

        // Registrar el fee cobrado (negativo porque es un cargo)
        ticket.setRefundAmount(noShowFee.negate());

        // Guardar cambios
        ticketRepository.save(ticket);

        // Publicar evento para notificar que el asiento está disponible
        eventPublisher.publishEvent(new SeatAvailableEvent(
                ticket.getTrip().getId(),
                ticket.getSeatNumber(),
                ticket.getFromStop().getId(),
                ticket.getToStop().getId()
        ));

        log.info("Ticket {} marked as NO_SHOW. Seat {} released for quick sale. Fee: {}",
                ticket.getId(), ticket.getSeatNumber(), noShowFee);
    }

    @Override
    public void processUpcomingTripsNoShow() {
        Integer noShowWindow = configService.getConfigValueAsInt("no.show.window.minutes", 5);
        LocalDateTime checkTime = LocalDateTime.now().plusMinutes(noShowWindow);

        // Buscar viajes en BOARDING que salen dentro de la ventana de tiempo
        List<Trip> upcomingTrips = tripRepository.findByStatusAndDepartureAtBefore(
                TripStatus.BOARDING, checkTime);

        log.info("Found {} trips to check for no-show", upcomingTrips.size());

        for (Trip trip : upcomingTrips) {
            try {
                processNoShowTickets(trip.getId());
            } catch (Exception e) {
                log.error("Error processing no-show for trip {}: {}", trip.getId(), e.getMessage());
            }
        }
    }


    public void releaseNoShowSeat(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        if (ticket.getStatus() != TicketStatus.NO_SHOW) {
            throw new IllegalStateException("Ticket is not in NO_SHOW status");
        }

        // El asiento ya está liberado al marcar como NO_SHOW
        log.info("Seat {} from ticket {} is available for quick sale",
                ticket.getSeatNumber(), ticketId);
    }

    @Override
    public BigDecimal calculateNoShowFee(Ticket ticket) {

        // 1. Fee fijo
        BigDecimal fixedFee = new BigDecimal(
                configService.getConfigValue("no.show.fee.fixed", "5000")
        );

        // 2. Porcentaje
        double percentage = configService.getConfigValueAsDouble("no.show.fee.percentage", 10.0);
        BigDecimal percentFee = ticket.getPrice()
                .multiply(BigDecimal.valueOf(percentage / 100.0));

        // 3. Fee final = el mayor entre fijo y porcentual
        BigDecimal finalFee = fixedFee.max(percentFee);

        log.debug("NoShowFee calculated -> fixed: {}, percent: {}, final: {}",
                fixedFee, percentFee, finalFee);

        return finalFee;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableSeatsFromNoShow(Long tripId) {
        return ticketRepository.findByTripIdAndStatus(tripId, TicketStatus.NO_SHOW)
                .stream()
                .map(Ticket::getSeatNumber)
                .distinct()
                .collect(Collectors.toList());
    }
}
