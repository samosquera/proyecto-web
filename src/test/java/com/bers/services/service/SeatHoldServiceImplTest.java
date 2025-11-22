package com.bers.services.service;

import com.bers.api.dtos.SeatHoldDtos.*;
import com.bers.domain.entities.*;
import com.bers.domain.entities.enums.*;
import com.bers.domain.repositories.*;
import com.bers.services.mappers.SeatHoldMapper;
import com.bers.services.service.serviceImple.SeatHoldServiceImpl;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class SeatHoldServiceImplTest {
    @Mock
    private SeatHoldRepository seatHoldRepository;
    @Mock
    private TripRepository tripRepository;
    @Mock
    private UserRepository userRepository;
    @Spy
    private SeatHoldMapper seatHoldMapper = Mappers.getMapper(SeatHoldMapper.class);
    @InjectMocks
    private SeatHoldServiceImpl seatHoldService;
    private SeatHold seatHold;
    private Trip trip;
    private User user;
    private SeatHoldCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        trip = Trip.builder()
                .id(1L)
                .date(LocalDate.now())
                .departureAt(LocalDateTime.now())
                .route(Route.builder().id(1L).name("Test").build())
                .build();
        user = User.builder()
                .id(1L)
                .username("John Doe")
                .build();
        seatHold = SeatHold.builder()
                .id(1L)
                .trip(trip)
                .user(user)
                .seatNumber("1A")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .status(HoldStatus.HOLD)
                .createdAt(LocalDateTime.now())
                .build();
        createRequest = new SeatHoldCreateRequest(1L, "1A", 1L, 2L);
    }

    @Test
    @DisplayName("Debe crear un seathold")
    void shouldCreateSeatHoldSuccessfully() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(seatHoldRepository.existsByTripIdAndSeatNumberAndStatus(
                any(), any(), any())).thenReturn(false);
        when(seatHoldRepository.save(any())).thenReturn(seatHold);

        SeatHoldResponse result = seatHoldService.createSeatHold(createRequest, 1L);

        assertNotNull(result);
        Assertions.assertEquals("1A", result.seatNumber());
        verify(seatHoldRepository).save(any(SeatHold.class));
        verify(seatHoldMapper).toEntity(createRequest);
        verify(seatHoldMapper).toResponse(any(SeatHold.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el seatHold ya está held")
    void shouldThrowExceptionWhenSeatAlreadyHeld() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(seatHoldRepository.existsByTripIdAndSeatNumberAndStatus(
                any(), any(), any())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> seatHoldService.createSeatHold(createRequest, 1L)
        );

        Assertions.assertTrue(exception.getMessage().contains("already held"));
        verify(seatHoldRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe expirar holds old")
    void shouldExpireOldHolds() {
        List<SeatHold> expiredHolds = Arrays.asList(seatHold, seatHold);
        when(seatHoldRepository.findExpiredHolds(any())).thenReturn(expiredHolds);
        when(seatHoldRepository.save(any())).thenReturn(seatHold);

        seatHoldService.expireOldHolds();

        verify(seatHoldRepository).findExpiredHolds(any(LocalDateTime.class));
        verify(seatHoldRepository, times(2)).save(any(SeatHold.class));
        expiredHolds.forEach(hold ->
                Assertions.assertEquals(HoldStatus.EXPIRED, hold.getStatus())
        );
    }

    @Test
    @DisplayName("Debe liberar un seathold")
    void shouldReleaseSeatHold() {
        when(seatHoldRepository.findById(1L)).thenReturn(Optional.of(seatHold));
        when(seatHoldRepository.save(any())).thenReturn(seatHold);

        seatHoldService.releaseSeatHold(1L);

        Assertions.assertEquals(HoldStatus.EXPIRED, seatHold.getStatus());
        verify(seatHoldRepository).save(seatHold);
    }

    @Test
    @DisplayName("Debe convertir seatHold a ticket")
    void shouldConvertHoldToTicket() {
        when(seatHoldRepository.findById(1L)).thenReturn(Optional.of(seatHold));
        when(seatHoldRepository.save(any())).thenReturn(seatHold);

        seatHoldService.convertHoldToTicket(1L);

        Assertions.assertEquals(HoldStatus.CONVERTED, seatHold.getStatus());
        verify(seatHoldRepository).save(seatHold);
    }

    @Test
    @DisplayName("Debe lanzar excepción al liberar seatHold no active")
    void shouldThrowExceptionWhenReleasingNonActiveHold() {
        seatHold.setStatus(HoldStatus.EXPIRED);
        when(seatHoldRepository.findById(1L)).thenReturn(Optional.of(seatHold));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> seatHoldService.releaseSeatHold(1L)
        );

        Assertions.assertTrue(exception.getMessage().contains("Can only release active seatHolds"));
        verify(seatHoldRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe verificar si el seatHold está held")
    void shouldVerifyIfSeatIsHeld() {
        when(seatHoldRepository.existsByTripIdAndSeatNumberAndStatus(
                1L, "1A", HoldStatus.HOLD)).thenReturn(true);

        boolean result = seatHoldService.isSeatHeld(1L, "1A");

        Assertions.assertTrue(result);
    }
}