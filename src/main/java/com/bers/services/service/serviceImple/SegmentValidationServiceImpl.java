package com.bers.services.service.serviceImple;

import com.bers.domain.entities.Ticket;
import com.bers.domain.entities.enums.TicketStatus;
import com.bers.domain.repositories.TicketRepository;
import com.bers.services.service.SegmentValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del servicio de validación de segmentos.
 * Controla la disponibilidad de asientos entre paradas intermedias.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SegmentValidationServiceImpl implements SegmentValidationService {

    private final TicketRepository ticketRepository;

    @Override
    public boolean isSeatAvailableForSegment(Long tripId, String seatNumber, Integer fromStopOrder, Integer toStopOrder) {
        List<Ticket> existingTickets = ticketRepository.findByTripIdAndSeatNumber(tripId, seatNumber);

        for (Ticket existingTicket : existingTickets) {
            if (isSegmentOverlap(existingTicket, fromStopOrder, toStopOrder)) {
                log.debug("[SegmentValidation] Overlap found for seat {} between stops {}→{}",
                        seatNumber, fromStopOrder, toStopOrder);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isSegmentOverlap(Ticket existingTicket, Integer newFromOrder, Integer newToOrder) {
        Integer existingFromOrder = existingTicket.getFromStop().getOrder();
        Integer existingToOrder = existingTicket.getToStop().getOrder();

        return (newFromOrder < existingToOrder) && (newToOrder > existingFromOrder);
    }

    @Override
    public void validateSegment(Long tripId, String seatNumber, Integer fromStopOrder, Integer toStopOrder) {
        if (!isSeatAvailableForSegment(tripId, seatNumber, fromStopOrder, toStopOrder)) {
            throw new IllegalArgumentException(
                    String.format("El asiento %s no está disponible para el tramo %d→%d. Hay solapamiento con otro ticket.",
                            seatNumber, fromStopOrder, toStopOrder));
        }
    }

    /**
     * Liberación parcial del asiento:
     * - Busca los tickets activos que llegan a o antes de la parada actual.
     * - Si el pasajero bajó en esa parada, marca el ticket como USED.
     * - Si el asiento queda libre para el resto del viaje, se registra el evento.
     */
    @Override
    @Transactional
    public void releaseSeatForSegment(Long tripId, String seatNumber, Integer stopOrder) {
        List<Ticket> activeTickets = ticketRepository.findActiveTicketsForStop(tripId, stopOrder);

        for (Ticket ticket : activeTickets) {
            if (seatNumber.equals(ticket.getSeatNumber())) {
                Integer passengerExitOrder = ticket.getToStop().getOrder();

                // Si el pasajero baja en esta parada, liberar el asiento
                if (passengerExitOrder.equals(stopOrder)) {
                    ticket.setStatus(TicketStatus.USED);
                    ticketRepository.save(ticket);

                    log.info("[SegmentValidation] Seat {} released at stop {} (ticket {})",
                            seatNumber, stopOrder, ticket.getId());
                }
                // Si el pasajero sigue más adelante, no liberar todavía
                else if (passengerExitOrder > stopOrder) {
                    log.debug("[SegmentValidation] Passenger in seat {} continues to stop {}",
                            seatNumber, passengerExitOrder);
                }
            }
        }
    }
}
