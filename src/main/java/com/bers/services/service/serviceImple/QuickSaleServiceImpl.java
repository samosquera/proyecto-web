package com.bers.services.service.serviceImple;

import com.bers.api.dtos.QuickSaleDtos.AvailableQuickSaleSeatsResponse;
import com.bers.api.dtos.QuickSaleDtos.QuickSaleRequest;
import com.bers.api.dtos.QuickSaleDtos.QuickSaleResponse;
import com.bers.api.dtos.TicketDtos.TicketCreateRequest;
import com.bers.domain.entities.FareRule;
import com.bers.domain.entities.Trip;
import com.bers.domain.entities.enums.TripStatus;
import com.bers.domain.repositories.FareRuleRepository;
import com.bers.domain.repositories.TicketRepository;
import com.bers.domain.repositories.TripRepository;
import com.bers.domain.repositories.UserRepository;
import com.bers.services.service.ConfigService;
import com.bers.services.service.NoShowService;
import com.bers.services.service.QuickSaleService;
import com.bers.services.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuickSaleServiceImpl implements QuickSaleService {

    private final TripRepository tripRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketService ticketService;
    private final NoShowService noShowService;
    private final ConfigService configService;
    private final FareRuleRepository fareRuleRepository;

    @Override
    public QuickSaleResponse createQuickSale(QuickSaleRequest request) {
        log.info("Processing quick sale for trip {} seat {}", request.tripId(), request.seatNumber());

        // Validar que el viaje existe y está próximo a salir
        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + request.tripId()));

        // Validar que el viaje está en estado correcto
        if (trip.getStatus() != TripStatus.BOARDING && trip.getStatus() != TripStatus.SCHEDULED) {
            throw new IllegalStateException("Trip is not available for quick sale");
        }

        // Validar tiempo hasta salida
        long minutesUntilDeparture = Duration.between(LocalDateTime.now(), trip.getDepartureAt()).toMinutes();
        Integer quickSaleWindow = configService.getConfigValueAsInt("quick.sale.window.minutes", 10);

        if (minutesUntilDeparture > quickSaleWindow) {
            throw new IllegalStateException("Quick sale only available " + quickSaleWindow + " minutes before departure");
        }

        if (minutesUntilDeparture < 0) {
            throw new IllegalStateException("Trip has already departed");
        }

        // Verificar que el asiento está disponible (puede ser de un NO_SHOW)
        boolean isNoShowSeat = noShowService.getAvailableSeatsFromNoShow(request.tripId())
                .contains(request.seatNumber());

        if (!isNoShowSeat && !ticketService.isSeatAvailable(request.tripId(), request.seatNumber())) {
            throw new IllegalArgumentException("Seat " + request.seatNumber() + " is not available");
        }

        // Calcular precio con descuento de venta rápida
        BigDecimal originalPrice = calculateOriginalPrice(trip.getRoute().getId(),
                request.fromStopId(), request.toStopId());

        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal finalPrice = originalPrice;

        if (Boolean.TRUE.equals(request.applyDiscount())) {
            Double discountPercentage = configService.getConfigValueAsDouble(
                    "quick.sale.discount.percentage", 20.0);
            discount = originalPrice.multiply(BigDecimal.valueOf(discountPercentage / 100))
                    .setScale(2, RoundingMode.HALF_UP);
            finalPrice = originalPrice.subtract(discount);
        }

        // Crear el ticket usando el servicio existente
        TicketCreateRequest ticketRequest = new TicketCreateRequest(
                request.tripId(),
                request.passengerId(),
                request.fromStopId(),
                request.toStopId(),
                request.seatNumber(),
                request.paymentMethod()
        );

        var ticketResponse = ticketService.createTicket(ticketRequest);

        log.info("Quick sale completed. Ticket {} created with price {} (discount: {})",
                ticketResponse.id(), finalPrice, discount);

        return new QuickSaleResponse(
                ticketResponse.id(),
                request.seatNumber(),
                originalPrice,
                discount,
                finalPrice,
                ticketResponse.qrCode(),
                LocalDateTime.now(),
                (int) minutesUntilDeparture,
                "Quick sale successful! Boarding closes in " + minutesUntilDeparture + " minutes"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AvailableQuickSaleSeatsResponse getAvailableQuickSaleSeats(Long tripId) {
        Trip trip = tripRepository.findByIdWithDetails(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + tripId));

        long minutesUntilDeparture = Duration.between(LocalDateTime.now(), trip.getDepartureAt()).toMinutes();

        // Obtener asientos disponibles de NO_SHOW
        List<String> availableSeats = noShowService.getAvailableSeatsFromNoShow(tripId);

        // También incluir asientos nunca vendidos
        if (trip.getBus() != null) {
            int capacity = trip.getBus().getCapacity();
            for (int i = 1; i <= capacity; i++) {
                String seatNumber = String.valueOf(i);
                if (ticketService.isSeatAvailable(tripId, seatNumber)) {
                    if (!availableSeats.contains(seatNumber)) {
                        availableSeats.add(seatNumber);
                    }
                }
            }
        }

        // Calcular precio de venta rápida
        BigDecimal basePrice = new BigDecimal("50000"); // Precio default
        Double discountPercentage = configService.getConfigValueAsDouble(
                "quick.sale.discount.percentage", 20.0);
        BigDecimal quickSalePrice = basePrice.multiply(
                        BigDecimal.valueOf(1 - discountPercentage / 100))
                .setScale(2, RoundingMode.HALF_UP);

        String routeInfo = trip.getRoute().getOrigin() + " → " + trip.getRoute().getDestination();

        return new AvailableQuickSaleSeatsResponse(
                tripId,
                routeInfo,
                trip.getDepartureAt(),
                (int) minutesUntilDeparture,
                availableSeats,
                quickSalePrice
        );
    }

    private BigDecimal calculateOriginalPrice(Long routeId, Long fromStopId, Long toStopId) {
        return fareRuleRepository.findFareForSegment(routeId, fromStopId, toStopId)
                .map(FareRule::getBasePrice)
                .orElse(new BigDecimal("50000"));
    }
}
