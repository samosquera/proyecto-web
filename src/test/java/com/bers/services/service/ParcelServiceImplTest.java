package com.bers.services.service;

import com.bers.api.dtos.ParcelDtos.*;
import com.bers.domain.entities.Parcel;
import com.bers.domain.entities.Stop;
import com.bers.domain.entities.Trip;
import com.bers.domain.entities.enums.ParcelStatus;
import com.bers.domain.repositories.ParcelRepository;
import com.bers.domain.repositories.StopRepository;
import com.bers.domain.repositories.TripRepository;
import com.bers.services.mappers.ParcelMapper;
import com.bers.services.service.serviceImple.ParcelServiceImpl;
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

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParcelService test")
class ParcelServiceImplTest {
    @Mock
    private ParcelRepository parcelRepository;
    @Mock
    private TripRepository tripRepository;
    @Mock
    private StopRepository stopRepository;
    @Spy
    private ParcelMapper parcelMapper = Mappers.getMapper(ParcelMapper.class);
    @InjectMocks
    private ParcelServiceImpl parcelService;
    private Parcel parcel;
    private Stop fromStop;
    private Stop toStop;
    private ParcelCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        fromStop = Stop.builder().id(1L).name("Bogotá").order(0).build();
        toStop = Stop.builder().id(2L).name("Tunja").order(1).build();
        parcel = Parcel.builder()
                .id(1L)
                .code("PCL-123")
                .senderName("John")
                .senderPhone("3001234567")
                .receiverName("Jane")
                .receiverPhone("3009876543")
                .price(new BigDecimal("25000"))
                .status(ParcelStatus.CREATED)
                .deliveryOtp("123456")
                .fromStop(fromStop)
                .toStop(toStop)
                .build();
        createRequest = new ParcelCreateRequest(
                "John", "3001234567", "Jane", "3009876543",
                new BigDecimal("25000"), 1L, 2L, null
        );
    }

    @Test
    @DisplayName("Debe crear una parcel")
    void shouldCreateParcelSuccessfully() {
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
        when(parcelRepository.save(any())).thenReturn(parcel);

        ParcelResponse result = parcelService.createParcel(createRequest);

        assertNotNull(result);
        assertNotNull(result.code());
        Assertions.assertTrue(result.code().startsWith("PCL-"));
        verify(parcelRepository).save(any(Parcel.class));
        verify(parcelMapper).toEntity(createRequest);
        verify(parcelMapper).toResponse(any(Parcel.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción con stop sequence inválida")
    void shouldThrowExceptionWithInvalidStopSequence() {
        Stop invalidStop = Stop.builder().id(3L).order(2).build();
        when(stopRepository.findById(1L)).thenReturn(Optional.of(invalidStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parcelService.createParcel(createRequest)
        );

        Assertions.assertTrue(exception.getMessage().contains("Invalid stop sequence"));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe marcar parcel como in_transit")
    void shouldMarkParcelAsInTransit() {
        Trip trip = Trip.builder().id(1L).build();
        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(parcelRepository.save(any())).thenReturn(parcel);

        ParcelResponse result = parcelService.markAsInTransit(1L, 1L);

        assertNotNull(result);
        Assertions.assertEquals(ParcelStatus.IN_TRANSIT, parcel.getStatus());
        verify(parcelRepository).save(parcel);
    }

    @Test
    @DisplayName("Debe marcar parcel como delivered con OTP válido")
    void shouldMarkParcelAsDelivered() {
        parcel.setStatus(ParcelStatus.IN_TRANSIT);
        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));
        when(parcelRepository.save(any())).thenReturn(parcel);

        ParcelResponse result = parcelService.markAsDelivered(
                1L, "123456", "https://photo.url",1L
        );

        assertNotNull(result);
        Assertions.assertEquals(ParcelStatus.DELIVERED, parcel.getStatus());
        Assertions.assertEquals("https://photo.url", parcel.getProofPhotoUrl());
        assertNotNull(parcel.getDeliveredAt());
    }

    @Test
    @DisplayName("Debe lanzar excepción con OTP inválido")
    void shouldThrowExceptionWithInvalidOtp() {
        parcel.setStatus(ParcelStatus.IN_TRANSIT);
        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parcelService.markAsDelivered(1L, "999999", "url",1L)
        );

        Assertions.assertTrue(exception.getMessage().contains("Invalid OTP"));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe validar OTP correctamente")
    void shouldValidateOtpCorrectly() {
        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));

        boolean result = parcelService.validateOtp(1L, "123456");

        Assertions.assertTrue(result);
    }
}