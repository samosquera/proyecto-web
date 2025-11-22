package com.bers.services.service.serviceImple;

import com.bers.domain.entities.Ticket;
import com.bers.domain.entities.enums.CancellationPolicy;
import com.bers.domain.entities.enums.TicketStatus;
import com.bers.domain.repositories.TicketRepository;
import com.bers.services.service.CancellationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Implementación del servicio de cancelaciones.
 * Evalúa las condiciones de tiempo y estado del ticket para determinar
 * la política aplicable y el monto de reembolso.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CancellationServiceImpl implements CancellationService {

    private final TicketRepository ticketRepository;

    /**
     * Determina la política de cancelación en función de las horas restantes
     * antes de la salida del viaje.
     */
    @Override
    public CancellationPolicy determineCancellationPolicy(Ticket ticket) {
        if (ticket == null || ticket.getTrip() == null) {
            throw new IllegalArgumentException("El ticket o el viaje asociado no puede ser nulo");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departureTime = ticket.getTrip().getDepartureAt();

        long hoursUntilDeparture = Duration.between(now, departureTime).toHours();

        CancellationPolicy policy;
        if (hoursUntilDeparture >= 24) {
            policy = CancellationPolicy.FULL_REFUND;
        } else if (hoursUntilDeparture >= 12) {
            policy = CancellationPolicy.PARTIAL_REFUND;
        } else {
            policy = CancellationPolicy.NO_REFUND;
        }

        log.debug("[CancellationService] Policy determined: {} ({}h before departure)", policy, hoursUntilDeparture);
        return policy;
    }

    /**
     * Calcula el monto del reembolso según la política aplicable.
     */
    @Override
    public BigDecimal calculateRefundAmount(Ticket ticket, LocalDateTime cancellationTime) {
        if (ticket == null) {
            throw new IllegalArgumentException("El ticket no puede ser nulo");
        }

        CancellationPolicy policy = determineCancellationPolicy(ticket);
        BigDecimal basePrice = ticket.getPrice() != null ? ticket.getPrice() : BigDecimal.ZERO;

        BigDecimal refundAmount = switch (policy) {
            case FULL_REFUND -> basePrice;
            case PARTIAL_REFUND -> basePrice.multiply(new BigDecimal("0.5"));
            case NO_REFUND -> BigDecimal.ZERO;
        };

        log.info("[CancellationService] Refund calculated: {} (Policy: {}, Ticket ID: {})",
                refundAmount, policy, ticket.getId());

        return refundAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Verifica si un ticket puede ser cancelado.
     * No se permite cancelar si:
     * - El viaje ya partió.
     * - El ticket ya fue usado.
     */
    @Override
    public boolean canCancelTicket(Ticket ticket) {
        if (ticket == null || ticket.getTrip() == null) return false;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departureTime = ticket.getTrip().getDepartureAt();

        if (now.isAfter(departureTime)) {
            log.warn("[CancellationService] Ticket {} cannot be cancelled: trip already departed", ticket.getId());
            return false;
        }

        if (ticket.getStatus() == TicketStatus.USED) {
            log.warn("[CancellationService] Ticket {} cannot be cancelled: already used", ticket.getId());
            return false;
        }

        return true;
    }

    /**
     * Devuelve el motivo por el cual un ticket no puede cancelarse.
     */
    @Override
    public String getCancellationReason(Ticket ticket) {
        if (ticket == null || ticket.getTrip() == null) return "El ticket o viaje no es válido";

        if (LocalDateTime.now().isAfter(ticket.getTrip().getDepartureAt())) {
            return "No se puede cancelar un ticket para un viaje que ya partió.";
        }

        if (ticket.getStatus() == TicketStatus.USED) {
            return "No se puede cancelar un ticket que ya fue usado.";
        }

        return null;
    }
}
