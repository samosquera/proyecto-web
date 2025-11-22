package com.bers.services.service;

import com.bers.domain.entities.Ticket;

/**
 * Servicio encargado de validar la disponibilidad de asientos en tramos de un viaje.
 * Se utiliza para asegurar que los tramos de los pasajeros no se solapen
 * y que los asientos se liberen correctamente en paradas intermedias.
 */
public interface SegmentValidationService {

    /**
     * Verifica si un asiento está disponible para un tramo específico del viaje.
     */
    boolean isSeatAvailableForSegment(Long tripId, String seatNumber, Integer fromStopOrder, Integer toStopOrder);

    /**
     * Determina si un nuevo tramo se solapa con un ticket existente.
     */
    boolean isSegmentOverlap(Ticket existingTicket, Integer newFromOrder, Integer newToOrder);

    /**
     * Válida que el tramo solicitado no genere conflictos con otros tickets del viaje.
     * Si hay solapamiento, lanza una excepción con mensaje detallado.
     */
    void validateSegment(Long tripId, String seatNumber, Integer fromStopOrder, Integer toStopOrder);

    /**
     * Libera el asiento en un tramo del viaje cuando el pasajero ha bajado.
     * Actualiza el estado del ticket o asiento para reflejar la disponibilidad
     * parcial del asiento.
     */
    void releaseSeatForSegment(Long tripId, String seatNumber, Integer stopOrder);
}
