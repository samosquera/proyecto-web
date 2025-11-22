package com.bers.services.mappers;

import com.bers.api.dtos.TicketDtos.*;
import com.bers.domain.entities.*;
import com.bers.domain.entities.enums.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("TicketMapper Tests")
class TicketMapperTest {
    private TicketMapper ticketMapper;
    @BeforeEach
    void setUp() {
        ticketMapper = Mappers.getMapper(TicketMapper.class);
    }

    @Test
    @DisplayName("Debe mapear TicketCreateRequest a la entidad Ticket")
    void shouldMapCreateRequestToEntity() {
        TicketCreateRequest request = new TicketCreateRequest(
                1L,
                2L,
                3L,
                4L,
                "1A",
                PaymentMethod.CASH
        );

        Ticket ticket = ticketMapper.toEntity(request);

        assertNotNull(ticket);
        Assertions.assertEquals("1A", ticket.getSeatNumber());
        Assertions.assertEquals(PaymentMethod.CASH, ticket.getPaymentMethod());
        Assertions.assertEquals(TicketStatus.SOLD, ticket.getStatus());
        assertNotNull(ticket.getQrCode());
        Assertions.assertTrue(ticket.getQrCode().startsWith("TICKET-1-1A-"));
        assertNull(ticket.getId());
        assertNull(ticket.getPrice());
    }

    @Test
    @DisplayName("Debe generar códigos QR únicos")
    void shouldGenerateUniqueQrCodes() {
        TicketCreateRequest request = new TicketCreateRequest(
                1L, 2L, 3L, 4L, "1A", PaymentMethod.CASH
        );

        Ticket ticket1 = ticketMapper.toEntity(request);
        Ticket ticket2 = ticketMapper.toEntity(request);

       Assertions.assertEquals(ticket1.getQrCode(), ticket2.getQrCode());
    }

    @Test
    @DisplayName("Debe actualizar la entidad Ticket desde TicketUpdateRequest")
    void shouldUpdateEntityFromUpdateRequest() {
        Ticket existingTicket = Ticket.builder()
                .id(1L)
                .seatNumber("1A")
                .price(new BigDecimal("50000"))
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.SOLD)
                .qrCode("QR-123")
                .build();

        TicketUpdateRequest request = new TicketUpdateRequest(
                TicketStatus.CANCELLED
        );

        ticketMapper.updateEntity(request, existingTicket);

        Assertions.assertEquals(TicketStatus.CANCELLED, existingTicket.getStatus());
        Assertions.assertEquals("1A", existingTicket.getSeatNumber());
        Assertions.assertEquals(new BigDecimal("50000"), existingTicket.getPrice());
    }

    @Test
    @DisplayName("Debe mapear la entidad Ticket a TicketResponse")
    void shouldMapEntityToResponse() {
        Route route = Route.builder()
                .id(1L)
                .name("Bogotá - Tunja")
                .build();

        Trip trip = Trip.builder()
                .id(1L)
                .date(LocalDate.of(2025, 12, 25))
                .departureAt(LocalDateTime.of(2025, 12, 25, 8, 0))
                .route(route)
                .build();

        User passenger = User.builder()
                .id(2L)
                .username("John Doe")
                .build();

        Stop fromStop = Stop.builder()
                .id(3L)
                .name("Terminal Bogotá")
                .build();

        Stop toStop = Stop.builder()
                .id(4L)
                .name("Terminal Tunja")
                .build();

        LocalDateTime createdAt = LocalDateTime.now();

        Ticket ticket = Ticket.builder()
                .id(1L)
                .trip(trip)
                .passenger(passenger)
                .fromStop(fromStop)
                .toStop(toStop)
                .seatNumber("1A")
                .price(new BigDecimal("45000"))
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .qrCode("QR-123-456")
                .createdAt(createdAt)
                .build();

        TicketResponse response = ticketMapper.toResponse(ticket);

        assertNotNull(response);
        Assertions.assertEquals(1L, response.id());
        Assertions.assertEquals(1L, response.tripId());
        Assertions.assertEquals("2025-12-25", response.tripDate());
        Assertions.assertEquals("08:00", response.tripTime());
        Assertions.assertEquals(2L, response.passengerId());
        Assertions.assertEquals("John Doe", response.passengerName());
        Assertions.assertEquals(3L, response.fromStopId());
        Assertions.assertEquals("Terminal Bogotá", response.fromStopName());
        Assertions.assertEquals(4L, response.toStopId());
        Assertions.assertEquals("Terminal Tunja", response.toStopName());
        Assertions.assertEquals("1A", response.seatNumber());
        Assertions.assertEquals(new BigDecimal("45000"), response.price());
        Assertions.assertEquals("CARD", response.paymentMethod());
        Assertions.assertEquals("SOLD", response.status());
        Assertions.assertEquals("QR-123-456", response.qrCode());
        Assertions.assertEquals(createdAt, response.createdAt());
    }

    @Test
    @DisplayName("Debe formatear la fecha correctamente")
    void shouldFormatDateCorrectly() {
        Trip trip = Trip.builder()
                .id(1L)
                .date(LocalDate.of(2025, 1, 5))
                .departureAt(LocalDateTime.of(2025, 1, 5, 14, 30))
                .route(Route.builder().id(1L).build())
                .build();

        Ticket ticket = Ticket.builder()
                .id(1L)
                .trip(trip)
                .passenger(User.builder().id(1L).username("Test").build())
                .fromStop(Stop.builder().id(1L).name("A").build())
                .toStop(Stop.builder().id(2L).name("B").build())
                .seatNumber("1A")
                .price(BigDecimal.TEN)
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.SOLD)
                .qrCode("QR")
                .createdAt(LocalDateTime.now())
                .build();

        TicketResponse response = ticketMapper.toResponse(ticket);

        Assertions.assertEquals("2025-01-05", response.tripDate());
        Assertions.assertEquals("14:30", response.tripTime());
    }

    @Test
    @DisplayName("Debe mapear todos los tipos de PaymentMethod correctamente")
    void shouldMapAllPaymentMethodTypes() {
        for (PaymentMethod method : PaymentMethod.values()) {
            TicketCreateRequest request = new TicketCreateRequest(
                    1L, 2L, 3L, 4L, "1A", method
            );

            Ticket ticket = ticketMapper.toEntity(request);

            Assertions.assertEquals(method, ticket.getPaymentMethod());

            ticket.setId(1L);
            ticket.setPrice(BigDecimal.TEN);
            ticket.setCreatedAt(LocalDateTime.now());
            ticket.setTrip(Trip.builder().id(1L)
                    .date(LocalDate.now())
                    .departureAt(LocalDateTime.now())
                    .route(Route.builder().id(1L).build())
                    .build());
            ticket.setPassenger(User.builder().id(1L).username("Test").build());
            ticket.setFromStop(Stop.builder().id(1L).name("A").build());
            ticket.setToStop(Stop.builder().id(2L).name("B").build());

            TicketResponse response = ticketMapper.toResponse(ticket);
            Assertions.assertEquals(method.name(), response.paymentMethod());
        }
    }

    @Test
    @DisplayName("Debe mapear todos los tipos de TicketStatus correctamente")
    void shouldMapAllTicketStatusTypes() {
        for (TicketStatus status : TicketStatus.values()) {
            Ticket ticket = Ticket.builder()
                    .id(1L)
                    .trip(Trip.builder().id(1L)
                            .date(LocalDate.now())
                            .departureAt(LocalDateTime.now())
                            .route(Route.builder().id(1L).build())
                            .build())
                    .passenger(User.builder().id(1L).username("Test").build())
                    .fromStop(Stop.builder().id(1L).name("A").build())
                    .toStop(Stop.builder().id(2L).name("B").build())
                    .seatNumber("1A")
                    .price(BigDecimal.TEN)
                    .paymentMethod(PaymentMethod.CASH)
                    .status(status)
                    .qrCode("QR")
                    .createdAt(LocalDateTime.now())
                    .build();

            TicketResponse response = ticketMapper.toResponse(ticket);

            Assertions.assertEquals(status.name(), response.status());
        }
    }

    @Test
    @DisplayName("Debe manejar la generación de códigos QR para diferentes viajes")
    void shouldHandleQrCodeGenerationForDifferentTrips() {
        TicketCreateRequest request1 = new TicketCreateRequest(
                1L, 2L, 3L, 4L, "1A", PaymentMethod.CASH
        );
        TicketCreateRequest request2 = new TicketCreateRequest(
                5L, 2L, 3L, 4L, "2B", PaymentMethod.CARD
        );

        Ticket ticket1 = ticketMapper.toEntity(request1);
        Ticket ticket2 = ticketMapper.toEntity(request2);

        Assertions.assertTrue(ticket1.getQrCode().contains("TICKET-1-1A-"));
        Assertions.assertTrue(ticket2.getQrCode().contains("TICKET-5-2B-"));
       Assertions.assertEquals(ticket1.getQrCode(), ticket2.getQrCode());
    }

    @Test
    @DisplayName("Debe manejar diferentes números de asiento en el QR")
    void shouldHandleDifferentSeatNumbersInQr() {
        String[] seatNumbers = {"1A", "10B", "VIP-5", "PREF_1"};

        for (String seatNumber : seatNumbers) {
            TicketCreateRequest request = new TicketCreateRequest(
                    1L, 2L, 3L, 4L, seatNumber, PaymentMethod.CASH
            );

            Ticket ticket = ticketMapper.toEntity(request);
            Assertions.assertTrue(ticket.getQrCode().contains(seatNumber));
        }
    }

    @Test
    @DisplayName("Debe preservar todos los campos durante el ciclo completo de mapeo")
    void shouldPreserveAllFieldsDuringFullCycleMapping() {
        TicketCreateRequest request = new TicketCreateRequest(
                100L, 200L, 300L, 400L, "VIP-1", PaymentMethod.QR
        );

        Ticket ticket = ticketMapper.toEntity(request);
        ticket.setId(999L);
        ticket.setPrice(new BigDecimal("150000"));
        ticket.setCreatedAt(LocalDateTime.of(2025, 12, 25, 10, 30));
        ticket.setTrip(Trip.builder()
                .id(100L)
                .date(LocalDate.of(2025, 12, 25))
                .departureAt(LocalDateTime.of(2025, 12, 25, 14, 0))
                .route(Route.builder().id(1L).name("Test Route").build())
                .build());
        ticket.setPassenger(User.builder()
                .id(200L)
                .username("Premium User")
                .build());
        ticket.setFromStop(Stop.builder()
                .id(300L)
                .name("Start Point")
                .build());
        ticket.setToStop(Stop.builder()
                .id(400L)
                .name("End Point")
                .build());

        TicketResponse response = ticketMapper.toResponse(ticket);

        Assertions.assertEquals(999L, response.id());
        Assertions.assertEquals(100L, response.tripId());
        Assertions.assertEquals("2025-12-25", response.tripDate());
        Assertions.assertEquals("14:00", response.tripTime());
        Assertions.assertEquals(200L, response.passengerId());
        Assertions.assertEquals("Premium User", response.passengerName());
        Assertions.assertEquals(300L, response.fromStopId());
        Assertions.assertEquals("Start Point", response.fromStopName());
        Assertions.assertEquals(400L, response.toStopId());
        Assertions.assertEquals("End Point", response.toStopName());
        Assertions.assertEquals("VIP-1", response.seatNumber());
        Assertions.assertEquals(new BigDecimal("150000"), response.price());
        Assertions.assertEquals("QR", response.paymentMethod());
        Assertions.assertEquals("SOLD", response.status());
    }
}