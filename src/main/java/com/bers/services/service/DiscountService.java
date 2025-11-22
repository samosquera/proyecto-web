package com.bers.services.service;

import com.bers.domain.entities.enums.PassengerType;

import java.math.BigDecimal;

public interface DiscountService {

    /**
     * Calcula el monto del descuento aplicable a un pasajero según su tipo.
     */
    BigDecimal calculateDiscount(PassengerType passengerType, BigDecimal basePrice);

    /**
     * Calcula el precio final con descuento aplicado.
     */
    BigDecimal applyDiscount(PassengerType passengerType, BigDecimal basePrice);

    /**
     * Determina si la edad indicada es válida para el tipo de pasajero.
     *
     */
    boolean validatePassengerAge(PassengerType passengerType, Integer age);

    /**
     * Determina el tipo de pasajero según edad y condición de estudiante.
     */
    PassengerType determinePassengerType(Integer age, Boolean isStudent);
}
