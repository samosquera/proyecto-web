package com.bers.services.mappers;

import com.bers.api.dtos.TripDtos.*;
import com.bers.domain.entities.Bus;
import com.bers.domain.entities.Route;
import com.bers.domain.entities.Trip;
import com.bers.domain.entities.enums.BusStatus;
import com.bers.domain.entities.enums.TripStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("TripMapper Tests")
class TripMapperTest {

    private TripMapper tripMapper;

    @BeforeEach
    void setUp() {
        tripMapper = Mappers.getMapper(TripMapper.class);
    }

    @Test
    @DisplayName("Debe mapear TripCreateRequest a la entidad Trip")
    void shouldMapCreateRequestToEntity() {
        LocalDate date = LocalDate.of(2025, 12, 25);
        LocalDateTime departure = LocalDateTime.of(2025, 12, 25, 8, 0);
        LocalDateTime arrival = LocalDateTime.of(2025, 12, 25, 11, 0);

        TripCreateRequest request = new TripCreateRequest(
                date,
                departure,
                arrival,
                1L,
                2L
        );

        Trip trip = tripMapper.toEntity(request);

        assertNotNull(trip);
        Assertions.assertEquals(date, trip.getDate());
        Assertions.assertEquals(departure, trip.getDepartureAt());
        Assertions.assertEquals(arrival, trip.getArrivalEta());
        Assertions.assertEquals(TripStatus.SCHEDULED, trip.getStatus());
        assertNotNull(trip.getRoute());
        Assertions.assertEquals(1L, trip.getRoute().getId());
        assertNotNull(trip.getBus());
        Assertions.assertEquals(2L, trip.getBus().getId());
        assertNull(trip.getId());
    }

    @Test
    @DisplayName("Debe mapear TripCreateRequest sin asignación de bus")
    void shouldMapCreateRequestWithoutBus() {
        LocalDate date = LocalDate.of(2025, 12, 25);
        LocalDateTime departure = LocalDateTime.of(2025, 12, 25, 8, 0);
        LocalDateTime arrival = LocalDateTime.of(2025, 12, 25, 11, 0);

        TripCreateRequest request = new TripCreateRequest(
                date,
                departure,
                arrival,
                1L,
                null
        );

        Trip trip = tripMapper.toEntity(request);

        assertNotNull(trip);
        assertNull(trip.getBus());
    }

    @Test
    @DisplayName("Debe actualizar la entidad Trip desde TripUpdateRequest")
    void shouldUpdateEntityFromUpdateRequest() {
        Trip existingTrip = Trip.builder()
                .id(1L)
                .date(LocalDate.of(2025, 12, 25))
                .departureAt(LocalDateTime.of(2025, 12, 25, 8, 0))
                .arrivalEta(LocalDateTime.of(2025, 12, 25, 11, 0))
                .status(TripStatus.SCHEDULED)
                .build();

        LocalDateTime newDeparture = LocalDateTime.of(2025, 12, 25, 9, 0);
        LocalDateTime newArrival = LocalDateTime.of(2025, 12, 25, 12, 0);

        TripUpdateRequest request = new TripUpdateRequest(
                newDeparture,
                newArrival,
                3L,
                TripStatus.BOARDING
        );

        tripMapper.updateEntity(request, existingTrip);

        Assertions.assertEquals(newDeparture, existingTrip.getDepartureAt());
        Assertions.assertEquals(newArrival, existingTrip.getArrivalEta());
        Assertions.assertEquals(TripStatus.BOARDING, existingTrip.getStatus());
        Assertions.assertEquals(LocalDate.of(2025, 12, 25), existingTrip.getDate());
    }

    @Test
    @DisplayName("Debe mapear la entidad Trip a TripResponse")
    void shouldMapEntityToResponse() {
        Route route = Route.builder()
                .id(1L)
                .code("BOG-TUN")
                .name("Bogotá - Tunja")
                .origin("Bogotá")
                .destination("Tunja")
                .distanceKm(150)
                .durationMin(180)
                .build();

        Bus bus = Bus.builder()
                .id(2L)
                .plate("ABC123")
                .capacity(40)
                .amenities(new HashMap<>())
                .status(BusStatus.ACTIVE)
                .build();

        LocalDate date = LocalDate.of(2025, 12, 25);
        LocalDateTime departure = LocalDateTime.of(2025, 12, 25, 8, 0);
        LocalDateTime arrival = LocalDateTime.of(2025, 12, 25, 11, 0);

        Trip trip = Trip.builder()
                .id(1L)
                .date(date)
                .departureAt(departure)
                .arrivalEta(arrival)
                .status(TripStatus.SCHEDULED)
                .route(route)
                .bus(bus)
                .build();

        TripResponse response = tripMapper.toResponse(trip);

        assertNotNull(response);
        Assertions.assertEquals(1L, response.id());
        Assertions.assertEquals(date, response.date());
        Assertions.assertEquals(departure, response.departureAt());
        Assertions.assertEquals(arrival, response.arrivalEta());
        Assertions.assertEquals("SCHEDULED", response.status());
        Assertions.assertEquals(1L, response.routeId());
        Assertions.assertEquals("Bogotá - Tunja", response.routeName());
        Assertions.assertEquals("Bogotá", response.origin());
        Assertions.assertEquals("Tunja", response.destination());
        Assertions.assertEquals(2L, response.busId());
        Assertions.assertEquals("ABC123", response.busPlate());
        Assertions.assertEquals(40, response.capacity());
    }

    @Test
    @DisplayName("Debe mapear todos los tipos de TripStatus correctamente")
    void shouldMapAllTripStatusTypes() {
        for (TripStatus status : TripStatus.values()) {
            Trip trip = Trip.builder()
                    .id(1L)
                    .date(LocalDate.now())
                    .departureAt(LocalDateTime.now())
                    .arrivalEta(LocalDateTime.now().plusHours(3))
                    .status(status)
                    .route(Route.builder().id(1L).name("Test").build())
                    .bus(Bus.builder().id(1L).plate("TEST").capacity(40).build())
                    .build();

            TripResponse response = tripMapper.toResponse(trip);

            Assertions.assertEquals(status.name(), response.status());
        }
    }

    @Test
    @DisplayName("Debe manejar viajes sin asignación de bus")
    void shouldHandleTripWithoutBusAssignment() {
        Route route = Route.builder()
                .id(1L)
                .name("Test Route")
                .origin("A")
                .destination("B")
                .build();

        Trip trip = Trip.builder()
                .id(1L)
                .date(LocalDate.now())
                .departureAt(LocalDateTime.now())
                .arrivalEta(LocalDateTime.now().plusHours(2))
                .status(TripStatus.SCHEDULED)
                .route(route)
                .bus(null)
                .build();

        TripResponse response = tripMapper.toResponse(trip);

        assertNotNull(response);
        assertNull(response.busId());
        assertNull(response.busPlate());
        assertNull(response.capacity());
    }

    @Test
    @DisplayName("Debe manejar diferentes combinaciones de fecha y hora")
    void shouldHandleDifferentDateTimeCombinations() {
        LocalDate date1 = LocalDate.of(2025, 1, 1);
        LocalDateTime departure1 = LocalDateTime.of(2025, 1, 1, 0, 30);
        LocalDateTime arrival1 = LocalDateTime.of(2025, 1, 1, 3, 30);

        TripCreateRequest request1 = new TripCreateRequest(
                date1, departure1, arrival1, 1L, null
        );

        Trip trip1 = tripMapper.toEntity(request1);
        Assertions.assertEquals(departure1, trip1.getDepartureAt());
        Assertions.assertEquals(arrival1, trip1.getArrivalEta());

        LocalDate date2 = LocalDate.of(2025, 12, 31);
        LocalDateTime departure2 = LocalDateTime.of(2025, 12, 31, 23, 0);
        LocalDateTime arrival2 = LocalDateTime.of(2026, 1, 1, 2, 0);

        TripCreateRequest request2 = new TripCreateRequest(
                date2, departure2, arrival2, 1L, null
        );

        Trip trip2 = tripMapper.toEntity(request2);
        Assertions.assertEquals(departure2, trip2.getDepartureAt());
        Assertions.assertEquals(arrival2, trip2.getArrivalEta());
    }

    @Test
    @DisplayName("Debe preservar todos los campos durante el ciclo completo de mapeo")
    void shouldPreserveAllFieldsDuringFullCycleMapping() {
        LocalDate date = LocalDate.of(2025, 6, 15);
        LocalDateTime departure = LocalDateTime.of(2025, 6, 15, 14, 30);
        LocalDateTime arrival = LocalDateTime.of(2025, 6, 15, 18, 45);

        TripCreateRequest request = new TripCreateRequest(
                date, departure, arrival, 100L, 200L
        );

        Trip trip = tripMapper.toEntity(request);
        trip.setId(999L);
        trip.setRoute(Route.builder()
                .id(100L)
                .code("TEST")
                .name("Test Route")
                .origin("Origin City")
                .destination("Destination City")
                .build());
        trip.setBus(Bus.builder()
                .id(200L)
                .plate("TEST-999")
                .capacity(50)
                .build());

        TripResponse response = tripMapper.toResponse(trip);

        Assertions.assertEquals(999L, response.id());
        Assertions.assertEquals(date, response.date());
        Assertions.assertEquals(departure, response.departureAt());
        Assertions.assertEquals(arrival, response.arrivalEta());
        Assertions.assertEquals("SCHEDULED", response.status());
        Assertions.assertEquals(100L, response.routeId());
        Assertions.assertEquals("Test Route", response.routeName());
        Assertions.assertEquals("Origin City", response.origin());
        Assertions.assertEquals("Destination City", response.destination());
        Assertions.assertEquals(200L, response.busId());
        Assertions.assertEquals("TEST-999", response.busPlate());
        Assertions.assertEquals(50, response.capacity());
    }

    @Test
    @DisplayName("Debe manejar un viaje de corta duración en el mismo día")
    void shouldHandleSameDayTripWithShortDuration() {
        LocalDate date = LocalDate.now();
        LocalDateTime departure = LocalDateTime.now().withHour(10).withMinute(0);
        LocalDateTime arrival = departure.plusMinutes(45);

        TripCreateRequest request = new TripCreateRequest(
                date, departure, arrival, 1L, 1L
        );

        Trip trip = tripMapper.toEntity(request);

        Assertions.assertEquals(date, trip.getDate());
        Assertions.assertEquals(departure, trip.getDepartureAt());
        Assertions.assertEquals(arrival, trip.getArrivalEta());
    }
}