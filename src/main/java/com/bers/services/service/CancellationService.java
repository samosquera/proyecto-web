package com.bers.services.service;

import com.bers.domain.entities.Ticket;
import com.bers.domain.entities.enums.CancellationPolicy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Servicio encargado de gestionar políticas de cancelación y reembolsos de tickets.
 */
public interface CancellationService {

    /**
     * Determina la política de cancelación aplicable a un ticket en función del tiempo
     * restante antes de la salida del viaje.
     */
    CancellationPolicy determineCancellationPolicy(Ticket ticket);

    /**
     * Calcula el monto de reembolso aplicable al cancelar un ticket.
     */
    BigDecimal calculateRefundAmount(Ticket ticket, LocalDateTime cancellationTime);

    /**
     * Verifica si el ticket puede ser cancelado (según su estado y hora de salida del viaje).
     */
    boolean canCancelTicket(Ticket ticket);

    /**
     * Devuelve el motivo por el cual un ticket no puede cancelarse, si aplica.
     */
    String getCancellationReason(Ticket ticket);
}
