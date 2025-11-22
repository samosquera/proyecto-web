package com.bers.services.service;

import com.bers.api.dtos.RouteDtos.*;
import com.bers.api.dtos.StopDtos.StopResponse;
import com.bers.domain.entities.Route;
import com.bers.domain.entities.Stop;
import com.bers.domain.repositories.RouteRepository;
import com.bers.domain.repositories.StopRepository;
import com.bers.services.mappers.RouteMapper;
import com.bers.services.mappers.StopMapper;
import com.bers.services.service.serviceImple.RouteServiceImpl;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RouteService test")
class RouteServiceImplTest {
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private StopRepository stopRepository;
    @Spy
    private RouteMapper routeMapper = Mappers.getMapper(RouteMapper.class);
    @Spy
    private StopMapper stopMapper = Mappers.getMapper(StopMapper.class);
    @InjectMocks
    private RouteServiceImpl routeService;
    private Route route;
    private RouteCreateRequest createRequest;
    private RouteUpdateRequest updateRequest;
    private Stop stop;

    @BeforeEach
    void setUp() {
        route = Route.builder()
                .id(1L)
                .code("BOG-TUN")
                .name("Bogotá - Tunja")
                .origin("Bogotá")
                .destination("Tunja")
                .distanceKm(150)
                .durationMin(180)
                .build();

        createRequest = new RouteCreateRequest(
                "BOG-TUN",
                "Bogotá - Tunja",
                "Bogotá",
                "Tunja",
                150,
                180
        );

        updateRequest = new RouteUpdateRequest(
                "Bogotá - Tunja Express",
                160,
                170
        );

        stop = Stop.builder()
                .id(1L)
                .name("Terminal Bogotá")
                .order(0)
                .route(route)
                .build();
    }

    @Test
    @DisplayName("Debe crear una route")
    void shouldCreateRouteSuccessfully() {
        when(routeRepository.existsByCode(anyString())).thenReturn(false);
        when(routeRepository.save(any(Route.class))).thenReturn(route);

        RouteResponse result = routeService.createRoute(createRequest);

        assertNotNull(result);
        Assertions.assertEquals("BOG-TUN", result.code());
        verify(routeRepository).existsByCode(createRequest.code());
        verify(routeRepository).save(any(Route.class));
        verify(routeMapper).toEntity(createRequest);
        verify(routeMapper).toResponse(any(Route.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el code ya existe")
    void shouldThrowExceptionWhenCodeAlreadyExists() {
        when(routeRepository.existsByCode(anyString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> routeService.createRoute(createRequest)
        );

        Assertions.assertTrue(exception.getMessage().contains("Route code already exists"));
        verify(routeRepository).existsByCode(createRequest.code());
        verify(routeRepository, never()).save(any(Route.class));
    }

    @Test
    @DisplayName("Debe actualizar una route")
    void shouldUpdateRouteSuccessfully() {
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(routeRepository.save(any(Route.class))).thenReturn(route);

        RouteResponse result = routeService.updateRoute(1L, updateRequest);

        assertNotNull(result);
        verify(routeRepository).findById(1L);
        verify(routeMapper).updateEntity(updateRequest, route);
        verify(routeRepository).save(route);
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar route inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentRoute() {
        when(routeRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> routeService.updateRoute(1L, updateRequest)
        );

        Assertions.assertTrue(exception.getMessage().contains("Route not found"));
        verify(routeRepository).findById(1L);
        verify(routeRepository, never()).save(any(Route.class));
    }

    @Test
    @DisplayName("Debe obtener route por ID")
    void shouldGetRouteById() {
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));

        RouteResponse result = routeService.getRouteById(1L);

        assertNotNull(result);
        Assertions.assertEquals(1L, result.id());
        verify(routeRepository).findById(1L);
        verify(routeMapper).toResponse(route);
    }

    @Test
    @DisplayName("Debe obtener route por code")
    void shouldGetRouteByCode() {
        when(routeRepository.findByCode(anyString())).thenReturn(Optional.of(route));

        RouteResponse result = routeService.getRouteByCode("BOG-TUN");

        assertNotNull(result);
        Assertions.assertEquals("BOG-TUN", result.code());
        verify(routeRepository).findByCode("BOG-TUN");
    }

    @Test
    @DisplayName("Debe obtener ruta con paradas")
    void shouldGetRouteWithStops() {
        when(routeRepository.findByIdWithStops(1L)).thenReturn(Optional.of(route));

        RouteResponse result = routeService.getRouteWithStops(1L);

        assertNotNull(result);
        verify(routeRepository).findByIdWithStops(1L);
    }

    @Test
    @DisplayName("Debe obtener todas las routes")
    void shouldGetAllRoutes() {
        List<Route> routes = Arrays.asList(route, route);
        when(routeRepository.findAll()).thenReturn(routes);

        List<RouteResponse> result = routeService.getAllRoutes();

        assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        verify(routeRepository).findAll();
        verify(routeMapper, times(2)).toResponse(any(Route.class));
    }

    @Test
    @DisplayName("Debe buscar routes por origin y destination")
    void shouldSearchRoutesByOriginAndDestination() {
        List<Route> routes = Arrays.asList(route);
        when(routeRepository.findByOriginIgnoreCaseAndDestinationIgnoreCase(anyString(), anyString()))
                .thenReturn(routes);

        List<RouteResponse> result = routeService.searchRoutes("Bogotá", "Tunja");

        assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        verify(routeRepository).findByOriginIgnoreCaseAndDestinationIgnoreCase("Bogotá", "Tunja");
    }

    @Test
    @DisplayName("Debe buscar routes por término de búsqueda")
    void shouldSearchRoutesBySearchTerm() {
        List<Route> routes = Arrays.asList(route);
        when(routeRepository.findByOriginContainingIgnoreCaseOrDestinationContainingIgnoreCase(
                anyString(), anyString())).thenReturn(routes);

        List<RouteResponse> result = routeService.searchRoutes("Bogotá", null);

        assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        verify(routeRepository).findByOriginContainingIgnoreCaseOrDestinationContainingIgnoreCase(
                "Bogotá", "Bogotá");
    }

    @Test
    @DisplayName("Debe obtener stop por route")
    void shouldGetStopsByRoute() {
        List<Stop> stops = Arrays.asList(stop);
        when(routeRepository.existsById(1L)).thenReturn(true);
        when(stopRepository.findByRouteIdOrderByOrderAsc(1L)).thenReturn(stops);

        List<StopResponse> result = routeService.getStopsByRoute(1L);

        assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        verify(routeRepository).existsById(1L);
        verify(stopRepository).findByRouteIdOrderByOrderAsc(1L);
        verify(stopMapper, times(1)).toResponse(any(Stop.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al obtener stop de route inexistente")
    void shouldThrowExceptionWhenGettingStopsOfNonExistentRoute() {
        when(routeRepository.existsById(1L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> routeService.getStopsByRoute(1L)
        );

        Assertions.assertTrue(exception.getMessage().contains("Route not found"));
        verify(routeRepository).existsById(1L);
        verify(stopRepository, never()).findByRouteIdOrderByOrderAsc(anyLong());
    }

    @Test
    @DisplayName("Debe eliminar una route")
    void shouldDeleteRoute() {
        when(routeRepository.existsById(1L)).thenReturn(true);

        routeService.deleteRoute(1L);

        verify(routeRepository).existsById(1L);
        verify(routeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar route inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentRoute() {
        when(routeRepository.existsById(1L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> routeService.deleteRoute(1L)
        );

        Assertions.assertTrue(exception.getMessage().contains("Route not found"));
        verify(routeRepository).existsById(1L);
        verify(routeRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Debe verificar si existe code de route")
    void shouldVerifyIfRouteCodeExists() {
        when(routeRepository.existsByCode(anyString())).thenReturn(true);

        boolean result = routeService.existsByCode("BOG-TUN");

        Assertions.assertTrue(result);
        verify(routeRepository).existsByCode("BOG-TUN");
    }
}