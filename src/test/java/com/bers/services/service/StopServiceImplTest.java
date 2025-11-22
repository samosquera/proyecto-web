package com.bers.services.service;

import com.bers.api.dtos.StopDtos.*;
import com.bers.domain.entities.Route;
import com.bers.domain.entities.Stop;
import com.bers.domain.repositories.RouteRepository;
import com.bers.domain.repositories.StopRepository;
import com.bers.services.mappers.StopMapper;
import com.bers.services.service.serviceImple.StopServiceImpl;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StopServiceImpl Tests")
class StopServiceImplTest {
    @Mock
    private StopRepository stopRepository;
    @Mock
    private RouteRepository routeRepository;
    @Spy
    private StopMapper stopMapper = Mappers.getMapper(StopMapper.class);
    @InjectMocks
    private StopServiceImpl stopService;
    private Stop stop;
    private Route route;
    private StopCreateRequest createRequest;
    private StopUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        route = Route.builder()
                .id(1L)
                .name("Ruta Centro")
                .code("RC-001")
                .build();

        stop = Stop.builder()
                .id(1L)
                .name("Terminal Central")
                .order(1)
                .lat(new BigDecimal("4.6097100"))
                .lng(new BigDecimal("-74.0817500"))
                .route(route)
                .build();

        createRequest = new StopCreateRequest(
                "Terminal Central",
                1,
                new BigDecimal("4.6097100"),
                new BigDecimal("-74.0817500"),
                1L
        );

        updateRequest = new StopUpdateRequest("Terminal Norte", 2);
    }

    @Test
    @DisplayName("Debe crear un stop exitosamente")
    void shouldCreateStopSuccessfully() {
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.findByRouteIdOrderByOrderAsc(1L)).thenReturn(Collections.emptyList());
        when(stopRepository.save(any(Stop.class))).thenReturn(stop);

        StopResponse response = stopService.createStop(createRequest);

        assertNotNull(response);
        Assertions.assertEquals("Terminal Central", response.name());
        Assertions.assertEquals(1, response.order());
        verify(routeRepository).findById(1L);
        verify(stopRepository).save(any(Stop.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando route no existe al crear")
    void shouldThrowExceptionWhenRouteNotFoundOnCreate() {
        when(routeRepository.findById(999L)).thenReturn(Optional.empty());

        StopCreateRequest invalidRequest = new StopCreateRequest(
                "Terminal", 1, BigDecimal.ZERO, BigDecimal.ZERO, 999L
        );

        assertThrows(IllegalArgumentException.class,
                () -> stopService.createStop(invalidRequest));
        verify(stopRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe actualizar un stop exitosamente")
    void shouldUpdateStopSuccessfully() {
        when(stopRepository.findById(1L)).thenReturn(Optional.of(stop));
        when(stopRepository.findByRouteIdOrderByOrderAsc(1L)).thenReturn(Collections.emptyList());
        when(stopRepository.save(any(Stop.class))).thenReturn(stop);

        StopResponse response = stopService.updateStop(1L, updateRequest);

        assertNotNull(response);
        verify(stopMapper).updateEntity(updateRequest, stop);
        verify(stopRepository).save(stop);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando stop no existe al actualizar")
    void shouldThrowExceptionWhenStopNotFoundOnUpdate() {
        when(stopRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> stopService.updateStop(999L, updateRequest));
        verify(stopRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe obtener un stop por id")
    void shouldGetStopById() {
        when(stopRepository.findById(1L)).thenReturn(Optional.of(stop));

        StopResponse response = stopService.getStopById(1L);

        assertNotNull(response);
        Assertions.assertEquals(1L, response.id());
        Assertions.assertEquals("Terminal Central", response.name());
        Assertions.assertEquals("RC-001", response.routeCode());
        verify(stopRepository).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando stop no existe")
    void shouldThrowExceptionWhenStopNotFound() {
        when(stopRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> stopService.getStopById(999L));
    }

    @Test
    @DisplayName("Debe obtener todos los stops")
    void shouldGetAllStops() {
        Stop stop2 = Stop.builder()
                .id(2L)
                .name("Terminal Norte")
                .order(2)
                .route(route)
                .build();

        when(stopRepository.findAll()).thenReturn(List.of(stop, stop2));

        List<StopResponse> responses = stopService.getAllStops();

        Assertions.assertEquals(2, responses.size());
        verify(stopRepository).findAll();
    }

    @Test
    @DisplayName("Debe obtener stops por routeId ordenados")
    void shouldGetStopsByRouteIdOrdered() {
        Stop stop2 = Stop.builder().id(2L).name("Parada 2").order(2).route(route).build();
        Stop stop3 = Stop.builder().id(3L).name("Parada 3").order(3).route(route).build();

        when(routeRepository.existsById(1L)).thenReturn(true);
        when(stopRepository.findByRouteIdOrderByOrderAsc(1L))
                .thenReturn(List.of(stop, stop2, stop3));

        List<StopResponse> responses = stopService.getStopsByRouteId(1L);

        Assertions.assertEquals(3, responses.size());
        Assertions.assertEquals(1, responses.get(0).order());
        Assertions.assertEquals(2, responses.get(1).order());
        verify(stopRepository).findByRouteIdOrderByOrderAsc(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando route no existe al buscar stops")
    void shouldThrowExceptionWhenRouteNotFoundOnGetStops() {
        when(routeRepository.existsById(999L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> stopService.getStopsByRouteId(999L));
        verify(stopRepository, never()).findByRouteIdOrderByOrderAsc(any());
    }

    @Test
    @DisplayName("Debe buscar stops por nombre")
    void shouldSearchStopsByName() {
        Stop stop2 = Stop.builder()
                .id(2L)
                .name("Terminal Sur")
                .order(2)
                .route(route)
                .build();

        when(stopRepository.findByNameContainingIgnoreCase("terminal"))
                .thenReturn(List.of(stop, stop2));

        List<StopResponse> responses = stopService.searchStopsByName("terminal");

        Assertions.assertEquals(2, responses.size());
        verify(stopRepository).findByNameContainingIgnoreCase("terminal");
    }

    @Test
    @DisplayName("Debe eliminar un stop")
    void shouldDeleteStop() {
        when(stopRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> stopService.deleteStop(1L));
        verify(stopRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar stop inexistente")
    void shouldThrowExceptionOnDeleteWhenNotFound() {
        when(stopRepository.existsById(999L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> stopService.deleteStop(999L));
        verify(stopRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Debe validar order correctamente cuando no existe")
    void shouldValidateOrderSuccessfully() {
        when(stopRepository.findByRouteIdOrderByOrderAsc(1L))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> stopService.validateStopOrder(1L, 1));
        verify(stopRepository).findByRouteIdOrderByOrderAsc(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando order es negativo")
    void shouldThrowExceptionWhenOrderIsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> stopService.validateStopOrder(1L, -1));
        verify(stopRepository, never()).findByRouteIdOrderByOrderAsc(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando order ya existe para route")
    void shouldThrowExceptionWhenOrderExistsForRoute() {
        Stop existingStop = Stop.builder().id(2L).order(1).route(route).build();
        when(stopRepository.findByRouteIdOrderByOrderAsc(1L))
                .thenReturn(List.of(existingStop));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> stopService.validateStopOrder(1L, 1)
        );

        Assertions.assertTrue(exception.getMessage().contains("already exists"));
        verify(stopRepository).findByRouteIdOrderByOrderAsc(1L);
    }
}