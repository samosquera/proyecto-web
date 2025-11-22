package com.bers.services.service;

import com.bers.api.dtos.SeatDtos.*;
import com.bers.domain.entities.Bus;
import com.bers.domain.entities.Seat;
import com.bers.domain.entities.enums.BusStatus;
import com.bers.domain.entities.enums.SeatType;
import com.bers.domain.repositories.BusRepository;
import com.bers.domain.repositories.SeatRepository;
import com.bers.services.mappers.SeatMapper;
import com.bers.services.service.serviceImple.SeatServiceImpl;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeatServiceImpl Tests")
class SeatServiceImplTest {
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private BusRepository busRepository;
    @Spy
    private SeatMapper seatMapper = Mappers.getMapper(SeatMapper.class);
    @InjectMocks
    private SeatServiceImpl seatService;
    private Seat seat;
    private Bus bus;
    private SeatCreateRequest createRequest;
    private SeatUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        bus = Bus.builder()
                .id(1L)
                .plate("ABC-123")
                .capacity(40)
                .status(BusStatus.ACTIVE)
                .seats(new ArrayList<>())
                .build();

        seat = Seat.builder()
                .id(1L)
                .number("A1")
                .type(SeatType.STANDARD)
                .bus(bus)
                .build();

        createRequest = new SeatCreateRequest("A1", SeatType.STANDARD, 1L);
        updateRequest = new SeatUpdateRequest(SeatType.PREFERENTIAL);
    }

    @Test
    @DisplayName("Debe crear un seat exitosamente")
    void shouldCreateSeatSuccessfully() {
        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));
        when(seatRepository.findByBusIdAndNumber(1L, "A1")).thenReturn(Optional.empty());
        when(seatRepository.countByBusId(1L)).thenReturn(10L);
        when(seatRepository.save(any(Seat.class))).thenReturn(seat);

        SeatResponse response = seatService.createSeat(createRequest);

        assertNotNull(response);
        Assertions.assertEquals("A1", response.number());
        Assertions.assertEquals(1L, response.busId());
        verify(busRepository).findById(1L);
        verify(seatRepository).save(any(Seat.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando bus no existe al crear")
    void shouldThrowExceptionWhenBusNotFoundOnCreate() {
        when(busRepository.findById(999L)).thenReturn(Optional.empty());

        SeatCreateRequest invalidRequest = new SeatCreateRequest("A1", SeatType.STANDARD, 999L);

        assertThrows(IllegalArgumentException.class,
                () -> seatService.createSeat(invalidRequest));
        verify(seatRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando number ya existe en bus")
    void shouldThrowExceptionWhenSeatNumberExists() {
        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));
        when(seatRepository.findByBusIdAndNumber(1L, "A1")).thenReturn(Optional.of(seat));

        assertThrows(IllegalArgumentException.class,
                () -> seatService.createSeat(createRequest));
        verify(seatRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando se excede capacity del bus")
    void shouldThrowExceptionWhenBusCapacityExceeded() {
        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));
        when(seatRepository.findByBusIdAndNumber(1L, "A1")).thenReturn(Optional.empty());
        when(seatRepository.countByBusId(1L)).thenReturn(40L);

        assertThrows(IllegalArgumentException.class,
                () -> seatService.createSeat(createRequest));
        verify(seatRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe actualizar un seat exitosamente")
    void shouldUpdateSeatSuccessfully() {
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(seatRepository.save(any(Seat.class))).thenReturn(seat);

        SeatResponse response = seatService.updateSeat(1L, updateRequest);

        assertNotNull(response);
        verify(seatMapper).updateEntity(updateRequest, seat);
        verify(seatRepository).save(seat);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando seat no existe al actualizar")
    void shouldThrowExceptionWhenSeatNotFoundOnUpdate() {
        when(seatRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> seatService.updateSeat(999L, updateRequest));
        verify(seatRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe obtener un seat por id")
    void shouldGetSeatById() {
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));

        SeatResponse response = seatService.getSeatById(1L);

        assertNotNull(response);
        Assertions.assertEquals(1L, response.id());
        Assertions.assertEquals("A1", response.number());
        verify(seatRepository).findById(1L);
    }

    @Test
    @DisplayName("Debe obtener un seat por busId y number")
    void shouldGetSeatByBusAndNumber() {
        when(seatRepository.findByBusIdAndNumber(1L, "A1")).thenReturn(Optional.of(seat));

        SeatResponse response = seatService.getSeatByBusAndNumber(1L, "A1");

        assertNotNull(response);
        Assertions.assertEquals("A1", response.number());
        verify(seatRepository).findByBusIdAndNumber(1L, "A1");
    }

    @Test
    @DisplayName("Debe obtener todos los seats")
    void shouldGetAllSeats() {
        Seat seat2 = Seat.builder().id(2L).number("A2").type(SeatType.STANDARD).bus(bus).build();
        when(seatRepository.findAll()).thenReturn(List.of(seat, seat2));

        List<SeatResponse> responses = seatService.getAllSeats();

        Assertions.assertEquals(2, responses.size());
        verify(seatRepository).findAll();
    }

    @Test
    @DisplayName("Debe obtener seats por busId ordenados")
    void shouldGetSeatsByBusId() {
        Seat seat2 = Seat.builder().id(2L).number("A2").type(SeatType.STANDARD).bus(bus).build();
        when(busRepository.existsById(1L)).thenReturn(true);
        when(seatRepository.findByBusIdOrderByNumberAsc(1L)).thenReturn(List.of(seat, seat2));

        List<SeatResponse> responses = seatService.getSeatsByBusId(1L);

        Assertions.assertEquals(2, responses.size());
        verify(seatRepository).findByBusIdOrderByNumberAsc(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando bus no existe al buscar seats")
    void shouldThrowExceptionWhenBusNotFoundOnGetSeats() {
        when(busRepository.existsById(999L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> seatService.getSeatsByBusId(999L));
        verify(seatRepository, never()).findByBusIdOrderByNumberAsc(any());
    }

    @Test
    @DisplayName("Debe obtener seats por busId y type")
    void shouldGetSeatsByBusIdAndType() {
        when(busRepository.existsById(1L)).thenReturn(true);
        when(seatRepository.findByBusIdAndType(1L, SeatType.PREFERENTIAL))
                .thenReturn(List.of(seat));

        List<SeatResponse> responses = seatService.getSeatsByBusIdAndType(1L, SeatType.PREFERENTIAL);

        Assertions.assertEquals(1, responses.size());
        verify(seatRepository).findByBusIdAndType(1L, SeatType.PREFERENTIAL);
    }

    @Test
    @DisplayName("Debe eliminar un seat")
    void shouldDeleteSeat() {
        when(seatRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> seatService.deleteSeat(1L));
        verify(seatRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar seat inexistente")
    void shouldThrowExceptionOnDeleteWhenNotFound() {
        when(seatRepository.existsById(999L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> seatService.deleteSeat(999L));
        verify(seatRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Debe contar seats por bus")
    void shouldCountSeatsByBus() {
        when(seatRepository.countByBusId(1L)).thenReturn(25L);
        long count = seatService.countSeatsByBus(1L);
        Assertions.assertEquals(25L, count);
        verify(seatRepository).countByBusId(1L);
    }
}