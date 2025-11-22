package com.bers.services.service.serviceImple;

import com.bers.domain.entities.enums.PassengerType;
import com.bers.services.service.DiscountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

/**
 * Implementación del servicio de descuentos con reglas predeterminadas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountServiceImpl implements DiscountService {

    /**
     * Mapa de descuentos base según tipo de pasajero.
     * Las tasas se expresan en decimales (por ejemplo, 0.25 = 25%).
     */
    private static final Map<PassengerType, BigDecimal> DEFAULT_DISCOUNTS = new EnumMap<>(PassengerType.class);

    static {
        DEFAULT_DISCOUNTS.put(PassengerType.ADULT, BigDecimal.ZERO);
        DEFAULT_DISCOUNTS.put(PassengerType.CHILD, new BigDecimal("0.40"));   // 40%
        DEFAULT_DISCOUNTS.put(PassengerType.STUDENT, new BigDecimal("0.25")); // 25%
        DEFAULT_DISCOUNTS.put(PassengerType.SENIOR, new BigDecimal("0.30"));  // 30%
    }

    /**
     * Calcula el descuento monetario según tipo de pasajero.
     */
    @Override
    public BigDecimal calculateDiscount(PassengerType passengerType, BigDecimal basePrice) {
        if (passengerType == null || basePrice == null) return BigDecimal.ZERO;

        BigDecimal rate = DEFAULT_DISCOUNTS.getOrDefault(passengerType, BigDecimal.ZERO);
        BigDecimal discount = basePrice.multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP);

        log.debug("[DiscountService] Type={} Rate={} Discount={} BasePrice={}",
                passengerType, rate, discount, basePrice);

        return discount;
    }

    /**
     * Aplica el descuento y devuelve el precio final.
     */
    @Override
    public BigDecimal applyDiscount(PassengerType passengerType, BigDecimal basePrice) {
        if (basePrice == null) return BigDecimal.ZERO;
        BigDecimal discount = calculateDiscount(passengerType, basePrice);
        BigDecimal finalPrice = basePrice.subtract(discount).max(BigDecimal.ZERO).setScale(2, BigDecimal.ROUND_HALF_UP);

        log.debug("[DiscountService] Final price for {} after discount: {}", passengerType, finalPrice);
        return finalPrice;
    }

    /**
     * Válida que la edad esté dentro del rango permitido por tipo de pasajero.
     */
    @Override
    public boolean validatePassengerAge(PassengerType passengerType, Integer age) {
        if (age == null || passengerType == null) return true;

        boolean valid;
        switch (passengerType) {
            case CHILD -> valid = age >= 3 && age <= 12;
            case STUDENT -> valid = age >= 13 && age <= 25;
            case SENIOR -> valid = age >= 65;
            case ADULT -> valid = age >= 18 && age < 65;
            default -> valid = true;
        }

        if (!valid) {
            log.warn("[DiscountService] Invalid age {} for passenger type {}", age, passengerType);
        }

        return valid;
    }

    /**
     * Determina automáticamente el tipo de pasajero según edad y si es estudiante.
     */
    @Override
    public PassengerType determinePassengerType(Integer age, Boolean isStudent) {
        if (age == null) return PassengerType.ADULT;

        PassengerType result;
        if (Boolean.TRUE.equals(isStudent) && age >= 13 && age <= 25) {
            result = PassengerType.STUDENT;
        } else if (age >= 65) {
            result = PassengerType.SENIOR;
        } else if (age >= 3 && age <= 12) {
            result = PassengerType.CHILD;
        } else {
            result = PassengerType.ADULT;
        }

        log.debug("[DiscountService] Determined type={} for age={} isStudent={}", result, age, isStudent);
        return result;
    }
}
