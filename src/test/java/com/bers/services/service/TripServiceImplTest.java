package com.bers.services.service;

import com.bers.api.dtos.TripDtos.*;
import com.bers.domain.entities.Bus;
import com.bers.domain.entities.Route;
import com.bers.domain.entities.Trip;
import com.bers.domain.entities.enums.TripStatus;
import com.bers.domain.repositories.BusRepository;
import com.bers.domain.repositories.RouteRepository;
import com.bers.domain.repositories.TripRepository;
import com.bers.services.mappers.TripMapper;
import com.bers.services.service.serviceImple.TripServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripService test")
class TripServiceImplTest {
    @Mock
    private TripRepository tripRepository;
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private BusRepository busRepository;
    @Spy
    private TripMapper tripMapper = Mappers.getMapper(TripMapper.class);
    @InjectMocks
    private TripServiceImpl tripService;
    private Trip trip;
    private Route route;
    private Bus bus;
    private TripCreateRequest createRequest;
    private TripUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        route = Route.builder()
                .id(1L)
                .code("BOG-TUN")
                .name("Bogotá - Tunja")
                .build();

        bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .build();

        trip = Trip.builder()
                .id(1L)
                .date(LocalDate.of(2025, 12, 25))
                .departureAt(LocalDateTime.of(2025, 12, 25, 8, 0))
                .arrivalEta(LocalDateTime.of(2025, 12, 25, 11, 0))
                .status(TripStatus.SCHEDULED)
                .route(route)
                .bus(bus)
                .build();

        createRequest = new TripCreateRequest(
                LocalDate.of(2025, 12, 25),
                LocalDateTime.of(2025, 12, 25, 8, 0),
                LocalDateTime.of(2025, 12, 25, 11, 0),
                1L,
                1L
        );

        updateRequest = new TripUpdateRequest(
                LocalDateTime.of(2025, 12, 25, 9, 0),
                LocalDateTime.of(2025, 12, 25, 12, 0),
                1L,
                TripStatus.BOARDING
        );
    }

    @Test
    @DisplayName("Debe crear un trip")
    void shouldCreateTripSuccessfully() {
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));
        when(tripRepository.findActiveTripsByBusAndDate(any(), any()))
                .thenReturn(Collections.emptyList());
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);

        TripResponse result = tripService.createTrip(createRequest);

        assertNotNull(result);
        Assertions.assertEquals(1L, result.id());
        verify(routeRepository).findById(1L);
        verify(busRepository).findById(1L);
        verify(tripRepository).save(any(Trip.class));
        verify(tripMapper).toEntity(createRequest);
        verify(tripMapper).toResponse(any(Trip.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la route no existe")
    void shouldThrowExceptionWhenRouteNotFound() {
        when(routeRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tripService.createTrip(createRequest)
        );

        Assertions.assertTrue(exception.getMessage().contains("Route not found"));
        verify(routeRepository).findById(1L);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el bus no existe")
    void shouldThrowExceptionWhenBusNotFound() {
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(busRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tripService.createTrip(createRequest)
        );

        Assertions.assertTrue(exception.getMessage().contains("Bus not found"));
        verify(busRepository).findById(1L);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la hora de arrival es antes de departure")
    void shouldThrowExceptionWhenArrivalBeforeDeparture() {
        TripCreateRequest invalidRequest = new TripCreateRequest(
                LocalDate.of(2025, 12, 25),
                LocalDateTime.of(2025, 12, 25, 11, 0),
                LocalDateTime.of(2025, 12, 25, 8, 0),
                1L,
                1L
        );
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));
        when(tripRepository.findActiveTripsByBusAndDate(any(), any()))
                .thenReturn(Collections.emptyList());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tripService.createTrip(invalidRequest)
        );

        Assertions.assertTrue(exception.getMessage().contains("Arrival time must be after departure time"));
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    @DisplayName("Debe actualizar un trip exitosamente")
    void shouldUpdateTripSuccessfully() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);

        TripResponse result = tripService.updateTrip(1L, updateRequest);

        assertNotNull(result);
        verify(tripRepository).findById(1L);
        verify(tripMapper).updateEntity(updateRequest, trip);
        verify(tripRepository).save(trip);
    }

    @Test
    @DisplayName("Debe obtener trip por ID")
    void shouldGetTripById() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        TripResponse result = tripService.getTripById(1L);

        assertNotNull(result);
        Assertions.assertEquals(1L, result.id());
        verify(tripRepository).findById(1L);
        verify(tripMapper).toResponse(trip);
    }

    @Test
    @DisplayName("Debe obtener trip con details")
    void shouldGetTripWithDetails() {
        when(tripRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(trip));

        TripResponse result = tripService.getTripWithDetails(1L);

        assertNotNull(result);
        verify(tripRepository).findByIdWithDetails(1L);
    }

    @Test
    @DisplayName("Debe obtener trips por route y date")
    void shouldGetTripsByRouteAndDate() {
        List<Trip> trips = Arrays.asList(trip);
        when(routeRepository.existsById(1L)).thenReturn(true);
        when(tripRepository.findByRouteIdAndDate(1L, LocalDate.of(2025, 12, 25)))
                .thenReturn(trips);

        List<TripResponse> result = tripService.getTripsByRouteAndDate(
                1L, LocalDate.of(2025, 12, 25)
        );

        assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        verify(routeRepository).existsById(1L);
    }

    @Test
    @DisplayName("Debe obtener trips activos por bus")
    void shouldGetActiveTripsByBus() {
        List<Trip> trips = Arrays.asList(trip);
        when(busRepository.existsById(1L)).thenReturn(true);
        when(tripRepository.findActiveTripsByBusAndDate(1L, LocalDate.of(2025, 12, 25)))
                .thenReturn(trips);

        List<TripResponse> result = tripService.getActiveTripsByBus(
                1L, LocalDate.of(2025, 12, 25)
        );

        assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        verify(busRepository).existsById(1L);
    }

    @Test
    @DisplayName("Debe cambiar el trip status")
    void shouldChangeTripStatus() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);

        TripResponse result = tripService.changeTripStatus(1L, TripStatus.BOARDING);

        assertNotNull(result);
        verify(tripRepository).findById(1L);
        verify(tripRepository).save(trip);
        Assertions.assertEquals(TripStatus.BOARDING, trip.getStatus());
    }

    @Test
    @DisplayName("Debe lanzar excepción al cambiar status cancelado")
    void shouldThrowExceptionWhenChangingCancelledStatus() {
        trip.setStatus(TripStatus.CANCELLED);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tripService.changeTripStatus(1L, TripStatus.BOARDING)
        );

        Assertions.assertTrue(exception.getMessage().contains("Cannot change status from"));
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción con transición de status inválida")
    void shouldThrowExceptionWithInvalidStatusTransition() {
        trip.setStatus(TripStatus.SCHEDULED);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tripService.changeTripStatus(1L, TripStatus.ARRIVED)
        );

        Assertions.assertTrue(exception.getMessage().contains("Invalid status transition"));
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    @DisplayName("Debe eliminar un trip")
    void shouldDeleteTrip() {
        when(tripRepository.existsById(1L)).thenReturn(true);

        tripService.deleteTrip(1L);

        verify(tripRepository).existsById(1L);
        verify(tripRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Debe validar bus schedule sin conflicts")
    void shouldValidateBusScheduleWithoutConflicts() {
        when(tripRepository.findActiveTripsByBusAndDate(any(), any()))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() ->
                tripService.validateTripSchedule(
                        1L,
                        LocalDate.of(2025, 12, 25),
                        LocalDateTime.of(2025, 12, 25, 8, 0)
                )
        );
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando bus tiene schedule conflict")
    void shouldThrowExceptionWhenBusHasScheduleConflict() {
        Trip conflictingTrip = Trip.builder()
                .id(2L)
                .departureAt(LocalDateTime.of(2025, 12, 25, 7, 30))
                .arrivalEta(LocalDateTime.of(2025, 12, 25, 10, 30))
                .build();
        when(tripRepository.findActiveTripsByBusAndDate(any(), any()))
                .thenReturn(Arrays.asList(conflictingTrip));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tripService.validateTripSchedule(
                        1L,
                        LocalDate.of(2025, 12, 25),
                        LocalDateTime.of(2025, 12, 25, 8, 0)
                )
        );

        Assertions.assertTrue(exception.getMessage().contains("already scheduled"));
    }
}