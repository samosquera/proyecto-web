package com.bers.services.service;

import com.bers.api.dtos.BusDtos.*;
import com.bers.domain.entities.Bus;
import com.bers.domain.entities.Seat;
import com.bers.domain.entities.enums.BusStatus;
import com.bers.domain.repositories.BusRepository;
import com.bers.services.mappers.BusMapper;
import com.bers.services.service.serviceImple.BusServiceImpl;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusServiceImpl Tests")
class BusServiceImplTest {
    @Mock
    private BusRepository busRepository;
    @Spy
    private BusMapper busMapper = Mappers.getMapper(BusMapper.class);
    @InjectMocks
    private BusServiceImpl busService;
    private Bus bus;
    private BusCreateRequest createRequest;
    private BusUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        Map<String, Object> amenities = Map.of("wifi", true, "airConditioning", true);

        bus = Bus.builder()
                .id(1L)
                .plate("ABC-123")
                .capacity(40)
                .amenities(new HashMap<>(amenities))
                .status(BusStatus.ACTIVE)
                .seats(new ArrayList<>())
                .build();

        createRequest = new BusCreateRequest("ABC-123", 40, amenities, BusStatus.ACTIVE);
        updateRequest = new BusUpdateRequest(50, amenities, BusStatus.MAINTENANCE);
    }

    @Test
    @DisplayName("Debe crear un bus exitosamente")
    void shouldCreateBusSuccessfully() {
        when(busRepository.existsByPlate("ABC-123")).thenReturn(false);
        when(busRepository.save(any(Bus.class))).thenReturn(bus);

        BusResponse response = busService.createBus(createRequest);

        assertNotNull(response);
        Assertions.assertEquals("ABC-123", response.plate());
        verify(busRepository).save(any(Bus.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el plate ya existe")
    void shouldThrowExceptionWhenPlateExists() {
        when(busRepository.existsByPlate("ABC-123")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> busService.createBus(createRequest));
        verify(busRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe actualizar un bus exitosamente")
    void shouldUpdateBusSuccessfully() {
        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));
        when(busRepository.save(any(Bus.class))).thenReturn(bus);

        BusResponse response = busService.updateBus(1L, updateRequest);

        assertNotNull(response);
        verify(busMapper).updateEntity(updateRequest, bus);
        verify(busRepository).save(bus);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el bus no existe al actualizar")
    void shouldThrowExceptionOnUpdateWhenNotFound() {
        when(busRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> busService.updateBus(999L, updateRequest));
        verify(busRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe obtener un bus por id")
    void shouldGetBusById() {
        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));

        BusResponse response = busService.getBusById(1L);

        assertNotNull(response);
        Assertions.assertEquals(1L, response.id());
        verify(busRepository).findById(1L);
    }

    @Test
    @DisplayName("Debe obtener un bus con seats")
    void shouldGetBusWithSeats() {
        Seat seat = Seat.builder().id(1L).number("A1").build();
        bus.setSeats(List.of(seat));
        when(busRepository.findByIdWithSeats(1L)).thenReturn(Optional.of(bus));

        BusResponse response = busService.getBusWithSeats(1L);

        assertNotNull(response);
        verify(busRepository).findByIdWithSeats(1L);
    }

    @Test
    @DisplayName("Debe obtener un bus por plate")
    void shouldGetBusByPlate() {
        when(busRepository.findByPlate("ABC-123")).thenReturn(Optional.of(bus));

        BusResponse response = busService.getBusbyPlate("ABC-123");

        assertNotNull(response);
        Assertions.assertEquals("ABC-123", response.plate());
        verify(busRepository).findByPlate("ABC-123");
    }

    @Test
    @DisplayName("Debe obtener todos los buses")
    void shouldGetAllBuses() {
        Bus bus2 = Bus.builder().id(2L).plate("XYZ-789").capacity(50)
                .status(BusStatus.ACTIVE).build();
        when(busRepository.findAll()).thenReturn(List.of(bus, bus2));

        List<BusResponse> responses = busService.getAllBuses();

        Assertions.assertEquals(2, responses.size());
        verify(busRepository).findAll();
    }

    @Test
    @DisplayName("Debe obtener buses por status")
    void shouldGetBusesByStatus() {
        when(busRepository.findByStatus(BusStatus.ACTIVE)).thenReturn(List.of(bus));

        List<BusResponse> responses = busService.getBusesByStatus(BusStatus.ACTIVE);

        Assertions.assertEquals(1, responses.size());
        verify(busRepository).findByStatus(BusStatus.ACTIVE);
    }

    @Test
    @DisplayName("Debe obtener buses disponibles por capacidad")
    void shouldGetAvailableBuses() {
        when(busRepository.findAvailableBusesByCapacity(BusStatus.ACTIVE, 30))
                .thenReturn(List.of(bus));

        List<BusResponse> responses = busService.getAvailableBuses(30);

        Assertions.assertEquals(1, responses.size());
        verify(busRepository).findAvailableBusesByCapacity(BusStatus.ACTIVE, 30);
    }

    @Test
    @DisplayName("Debe eliminar un bus")
    void shouldDeleteBus() {
        when(busRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> busService.deleteBus(1L));
        verify(busRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar bus inexistente")
    void shouldThrowExceptionOnDeleteWhenNotFound() {
        when(busRepository.existsById(999L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> busService.deleteBus(999L));
        verify(busRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Debe verificar si existe un plate")
    void shouldCheckIfPlateExists() {
        when(busRepository.existsByPlate("ABC-123")).thenReturn(true);

        boolean exists = busService.existsByPlate("ABC-123");

        Assertions.assertTrue(exists);
        verify(busRepository).existsByPlate("ABC-123");
    }

    @Test
    @DisplayName("Debe cambiar el status del bus")
    void shouldChangeBusStatus() {
        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));
        when(busRepository.save(any(Bus.class))).thenReturn(bus);

        BusResponse response = busService.changeBusStatus(1L, BusStatus.MAINTENANCE);

        assertNotNull(response);
        verify(busRepository).save(bus);
    }

    @Test
    @DisplayName("Debe lanzar excepción al cambiar status de bus inexistente")
    void shouldThrowExceptionOnChangeStatusWhenNotFound() {
        when(busRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> busService.changeBusStatus(999L, BusStatus.INACTIVE));
        verify(busRepository, never()).save(any());
    }
}