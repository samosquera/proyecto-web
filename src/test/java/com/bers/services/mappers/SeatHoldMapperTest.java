package com.bers.services.mappers;

import com.bers.api.dtos.SeatHoldDtos.*;
import com.bers.domain.entities.*;
import com.bers.domain.entities.enums.HoldStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SeatHoldMapper Tests")
class SeatHoldMapperTest {
    private SeatHoldMapper seatHoldMapper;

    @BeforeEach
    void setUp() {
        seatHoldMapper = Mappers.getMapper(SeatHoldMapper.class);
    }

    @Test
    @DisplayName("Debe mapear SeatHoldCreateRequest a la entidad SeatHold")
    void shouldMapCreateRequestToEntity() {
        SeatHoldCreateRequest request = new SeatHoldCreateRequest(
                1L,
                "1A",
                2L,
                3L
        );

        SeatHold seatHold = seatHoldMapper.toEntity(request);

        assertNotNull(seatHold);
        Assertions.assertEquals("1A", seatHold.getSeatNumber());
        Assertions.assertEquals(HoldStatus.HOLD, seatHold.getStatus());
        assertNull(seatHold.getId());
        assertNull(seatHold.getUser());
        assertNull(seatHold.getExpiresAt());
    }

    @Test
    @DisplayName("Debe actualizar la entidad SeatHold desde SeatHoldUpdateRequest")
    void shouldUpdateEntityFromUpdateRequest() {
        SeatHold existingSeatHold = SeatHold.builder()
                .id(1L)
                .seatNumber("1A")
                .status(HoldStatus.HOLD)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        SeatHoldUpdateRequest request = new SeatHoldUpdateRequest(
                HoldStatus.EXPIRED
        );

        seatHoldMapper.updateEntity(request, existingSeatHold);

        Assertions.assertEquals(HoldStatus.EXPIRED, existingSeatHold.getStatus());
        Assertions.assertEquals("1A", existingSeatHold.getSeatNumber());
    }

    @Test
    @DisplayName("Debe mapear la entidad SeatHold a SeatHoldResponse")
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

        User user = User.builder()
                .id(2L)
                .username("John Doe")
                .build();

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
        LocalDateTime createdAt = LocalDateTime.now();

        SeatHold seatHold = SeatHold.builder()
                .id(1L)
                .trip(trip)
                .seatNumber("1A")
                .user(user)
                .expiresAt(expiresAt)
                .status(HoldStatus.HOLD)
                .createdAt(createdAt)
                .build();

        SeatHoldResponse response = seatHoldMapper.toResponse(seatHold);

        assertNotNull(response);
        Assertions.assertEquals(1L, response.id());
        Assertions.assertEquals("1A", response.seatNumber());
        Assertions.assertEquals(expiresAt, response.expiresAt());
        Assertions.assertEquals("HOLD", response.status());
        Assertions.assertEquals(createdAt, response.createdAt());
        Assertions.assertEquals(1L, response.tripId());
        Assertions.assertEquals(2L, response.userId());
        Assertions.assertEquals("2025-12-25", response.tripDate());
        Assertions.assertEquals("08:00", response.tripTime());
        Assertions.assertEquals("Bogotá - Tunja", response.routeName());
        assertNotNull(response.minutesLeft());
        Assertions.assertTrue(response.minutesLeft() >= 0);
    }

    @Test
    @DisplayName("Debe calcular correctamente los minutos restantes para una expiración futura")
    void shouldCalculateMinutesLeftForFutureExpiration() {
        LocalDateTime futureExpiration = LocalDateTime.now().plusMinutes(8);

        SeatHold seatHold = SeatHold.builder()
                .id(1L)
                .trip(Trip.builder().id(1L)
                        .date(LocalDate.now())
                        .departureAt(LocalDateTime.now())
                        .route(Route.builder().id(1L).name("Test").build())
                        .build())
                .seatNumber("1A")
                .user(User.builder().id(1L).build())
                .expiresAt(futureExpiration)
                .status(HoldStatus.HOLD)
                .createdAt(LocalDateTime.now())
                .build();

        SeatHoldResponse response = seatHoldMapper.toResponse(seatHold);

        assertNotNull(response.minutesLeft());
        Assertions.assertTrue(response.minutesLeft() >= 7 && response.minutesLeft() <= 8);
    }

    @Test
    @DisplayName("Debe devolver cero minutos restantes para una reserva expirada")
    void shouldReturnZeroMinutesLeftForExpiredHold() {
        LocalDateTime pastExpiration = LocalDateTime.now().minusMinutes(5);

        SeatHold seatHold = SeatHold.builder()
                .id(1L)
                .trip(Trip.builder().id(1L)
                        .date(LocalDate.now())
                        .departureAt(LocalDateTime.now())
                        .route(Route.builder().id(1L).name("Test").build())
                        .build())
                .seatNumber("1A")
                .user(User.builder().id(1L).build())
                .expiresAt(pastExpiration)
                .status(HoldStatus.EXPIRED)
                .createdAt(LocalDateTime.now())
                .build();

        SeatHoldResponse response = seatHoldMapper.toResponse(seatHold);

        Assertions.assertEquals(0, response.minutesLeft());
    }

    @Test
    @DisplayName("Debe manejar expiresAt nulo")
    void shouldHandleNullExpiresAt() {
        SeatHold seatHold = SeatHold.builder()
                .id(1L)
                .trip(Trip.builder().id(1L)
                        .date(LocalDate.now())
                        .departureAt(LocalDateTime.now())
                        .route(Route.builder().id(1L).name("Test").build())
                        .build())
                .seatNumber("1A")
                .user(User.builder().id(1L).build())
                .expiresAt(null)
                .status(HoldStatus.HOLD)
                .createdAt(LocalDateTime.now())
                .build();

        SeatHoldResponse response = seatHoldMapper.toResponse(seatHold);

        assertNull(response.minutesLeft());
    }

    @Test
    @DisplayName("Debe mapear todos los tipos de HoldStatus correctamente")
    void shouldMapAllHoldStatusTypes() {
        for (HoldStatus status : HoldStatus.values()) {
            SeatHold seatHold = SeatHold.builder()
                    .id(1L)
                    .trip(Trip.builder().id(1L)
                            .date(LocalDate.now())
                            .departureAt(LocalDateTime.now())
                            .route(Route.builder().id(1L).name("Test").build())
                            .build())
                    .seatNumber("1A")
                    .user(User.builder().id(1L).build())
                    .expiresAt(LocalDateTime.now().plusMinutes(10))
                    .status(status)
                    .createdAt(LocalDateTime.now())
                    .build();

            SeatHoldResponse response = seatHoldMapper.toResponse(seatHold);

            Assertions.assertEquals(status.name(), response.status());
        }
    }

    @Test
    @DisplayName("Debe formatear la fecha y hora correctamente")
    void shouldFormatDateAndTimeCorrectly() {
        Trip trip = Trip.builder()
                .id(1L)
                .date(LocalDate.of(2025, 3, 15))
                .departureAt(LocalDateTime.of(2025, 3, 15, 16, 45))
                .route(Route.builder().id(1L).name("Test Route").build())
                .build();

        SeatHold seatHold = SeatHold.builder()
                .id(1L)
                .trip(trip)
                .seatNumber("1A")
                .user(User.builder().id(1L).build())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .status(HoldStatus.HOLD)
                .createdAt(LocalDateTime.now())
                .build();

        SeatHoldResponse response = seatHoldMapper.toResponse(seatHold);

        Assertions.assertEquals("2025-03-15", response.tripDate());
        Assertions.assertEquals("16:45", response.tripTime());
    }

    @Test
    @DisplayName("Debe manejar diferentes números de asiento")
    void shouldHandleDifferentSeatNumbers() {
        String[] seatNumbers = {"1A", "10B", "VIP-1", "PREF_5", "25"};

        for (String seatNumber : seatNumbers) {
            SeatHoldCreateRequest request = new SeatHoldCreateRequest(
                    1L, seatNumber, 2L, 3L
            );

            SeatHold seatHold = seatHoldMapper.toEntity(request);
            Assertions.assertEquals(seatNumber, seatHold.getSeatNumber());
        }
    }

    @Test
    @DisplayName("Debe preservar todos los campos durante el ciclo completo de mapeo")
    void shouldPreserveAllFieldsDuringFullCycleMapping() {
        SeatHoldCreateRequest request = new SeatHoldCreateRequest(
                100L, "VIP-10", 200L, 300L
        );

        SeatHold seatHold = seatHoldMapper.toEntity(request);
        seatHold.setId(999L);

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(5);

        seatHold.setExpiresAt(expiresAt);
        seatHold.setCreatedAt(createdAt);
        seatHold.setUser(User.builder().id(500L).username("Test User").build());
        seatHold.setTrip(Trip.builder()
                .id(100L)
                .date(LocalDate.of(2025, 12, 31))
                .departureAt(LocalDateTime.of(2025, 12, 31, 23, 59))
                .route(Route.builder().id(1L).name("New Year Route").build())
                .build());

        SeatHoldResponse response = seatHoldMapper.toResponse(seatHold);

        Assertions.assertEquals(999L, response.id());
        Assertions.assertEquals("VIP-10", response.seatNumber());
        Assertions.assertEquals(expiresAt, response.expiresAt());
        Assertions.assertEquals("HOLD", response.status());
        Assertions.assertEquals(createdAt, response.createdAt());
        Assertions.assertEquals(100L, response.tripId());
        Assertions.assertEquals(500L, response.userId());
        Assertions.assertEquals("2025-12-31", response.tripDate());
        Assertions.assertEquals("23:59", response.tripTime());
        Assertions.assertEquals("New Year Route", response.routeName());
        assertNotNull(response.minutesLeft());
    }

    @Test
    @DisplayName("Debe manejar la expiración exacta de 10 minutos")
    void shouldHandleExact10MinuteExpiration() {
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        SeatHold seatHold = SeatHold.builder()
                .id(1L)
                .trip(Trip.builder().id(1L)
                        .date(LocalDate.now())
                        .departureAt(LocalDateTime.now())
                        .route(Route.builder().id(1L).name("Test").build())
                        .build())
                .seatNumber("1A")
                .user(User.builder().id(1L).build())
                .expiresAt(expiresAt)
                .status(HoldStatus.HOLD)
                .createdAt(LocalDateTime.now())
                .build();

        SeatHoldResponse response = seatHoldMapper.toResponse(seatHold);

        assertNotNull(response.minutesLeft());
        Assertions.assertTrue(response.minutesLeft() >= 9 && response.minutesLeft() <= 10);
    }
}