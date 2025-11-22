package com.bers.services.mappers;

import com.bers.api.dtos.StopDtos.*;
import com.bers.domain.entities.Route;
import com.bers.domain.entities.Stop;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("StopMapper Tests")
class StopMapperTest {
    private StopMapper stopMapper;

    @BeforeEach
    void setUp() {
        stopMapper = Mappers.getMapper(StopMapper.class);
    }

    @Test
    @DisplayName("Debe mapear StopCreateRequest a la entidad Stop")
    void shouldMapCreateRequestToEntity() {

        StopCreateRequest request = new StopCreateRequest(
                "Terminal Bogotá",
                0,
                new BigDecimal("4.6533"),
                new BigDecimal("-74.0836"),
                1L
        );

        Stop stop = stopMapper.toEntity(request);

        assertNotNull(stop);
        Assertions.assertEquals("Terminal Bogotá", stop.getName());
        Assertions.assertEquals(0, stop.getOrder());
        Assertions.assertEquals(new BigDecimal("4.6533"), stop.getLat());
        Assertions.assertEquals(new BigDecimal("-74.0836"), stop.getLng());
        assertNull(stop.getId());
        assertNotNull(stop.getRoute());
        Assertions.assertEquals(1L, stop.getRoute().getId());
    }

    @Test
    @DisplayName("Debe actualizar la entidad Stop desde StopUpdateRequest")
    void shouldUpdateEntityFromUpdateRequest() {
        Stop existingStop = Stop.builder()
                .id(1L)
                .name("Old Name")
                .order(0)
                .lat(new BigDecimal("4.0000"))
                .lng(new BigDecimal("-74.0000"))
                .build();

        StopUpdateRequest request = new StopUpdateRequest(
                "New Terminal",
                1
        );

        stopMapper.updateEntity(request, existingStop);

        Assertions.assertEquals("New Terminal", existingStop.getName());
        Assertions.assertEquals(1, existingStop.getOrder());

        Assertions.assertEquals(new BigDecimal("4.0000"), existingStop.getLat());
        Assertions.assertEquals(new BigDecimal("-74.0000"), existingStop.getLng());
    }

    @Test
    @DisplayName("Debe mapear la entidad Stop a StopResponse")
    void shouldMapEntityToResponse() {
        Route route = Route.builder()
                .id(1L)
                .code("BOG-TUN")
                .name("Bogotá - Tunja")
                .build();

        Stop stop = Stop.builder()
                .id(1L)
                .name("Terminal Bogotá")
                .order(0)
                .lat(new BigDecimal("4.6533"))
                .lng(new BigDecimal("-74.0836"))
                .route(route)
                .build();

        StopResponse response = stopMapper.toResponse(stop);

        assertNotNull(response);
        Assertions.assertEquals(1L, response.id());
        Assertions.assertEquals("Terminal Bogotá", response.name());
        Assertions.assertEquals(0, response.order());
        Assertions.assertEquals(new BigDecimal("4.6533"), response.lat());
        Assertions.assertEquals(new BigDecimal("-74.0836"), response.lng());
        Assertions.assertEquals(1L, response.routeId());
        Assertions.assertEquals("Bogotá - Tunja", response.routeName());
        Assertions.assertEquals("BOG-TUN", response.routeCode());
    }

    @Test
    @DisplayName("Debe manejar coordenadas con alta precisión")
    void shouldHandleHighPrecisionCoordinates() {
        StopCreateRequest request = new StopCreateRequest(
                "Precise Location",
                0,
                new BigDecimal("4.6533278"),
                new BigDecimal("-74.0836333"),
                1L
        );

        Stop stop = stopMapper.toEntity(request);

        assertNotNull(stop);
        Assertions.assertEquals(new BigDecimal("4.6533278"), stop.getLat());
        Assertions.assertEquals(new BigDecimal("-74.0836333"), stop.getLng());
    }

    @Test
    @DisplayName("Debe manejar diferentes órdenes de stop")
    void shouldHandleDifferentStopOrders() {
        for (int order = 0; order < 10; order++) {
            StopCreateRequest request = new StopCreateRequest(
                    "Stop " + order,
                    order,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    1L
            );

            Stop stop = stopMapper.toEntity(request);
            Assertions.assertEquals(order, stop.getOrder());
        }
    }

    @Test
    @DisplayName("Debe manejar valores de latitud extremos")
    void shouldHandleExtremeLatitudeValues() {
        StopCreateRequest request1 = new StopCreateRequest(
                "North Pole",
                0,
                new BigDecimal("90.0"),
                BigDecimal.ZERO,
                1L
        );

        Stop stop1 = stopMapper.toEntity(request1);

        Assertions.assertEquals(new BigDecimal("90.0"), stop1.getLat());

        StopCreateRequest request2 = new StopCreateRequest(
                "South Pole",
                1,
                new BigDecimal("-90.0"),
                BigDecimal.ZERO,
                1L
        );

        Stop stop2 = stopMapper.toEntity(request2);

        Assertions.assertEquals(new BigDecimal("-90.0"), stop2.getLat());
    }

    @Test
    @DisplayName("Debe manejar valores de longitud extremos")
    void shouldHandleExtremeLongitudeValues() {
        StopCreateRequest request1 = new StopCreateRequest(
                "East Point",
                0,
                BigDecimal.ZERO,
                new BigDecimal("180.0"),
                1L
        );

        Stop stop1 = stopMapper.toEntity(request1);

        Assertions.assertEquals(new BigDecimal("180.0"), stop1.getLng());

        StopCreateRequest request2 = new StopCreateRequest(
                "West Point",
                1,
                BigDecimal.ZERO,
                new BigDecimal("-180.0"),
                1L
        );

        Stop stop2 = stopMapper.toEntity(request2);

        Assertions.assertEquals(new BigDecimal("-180.0"), stop2.getLng());
    }

    @Test
    @DisplayName("Debe mapear la referencia a Route correctamente")
    void shouldMapRouteReferenceCorrectly() {

        Long routeId = 42L;
        StopCreateRequest request = new StopCreateRequest(
                "Test Stop",
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                routeId
        );

        Stop stop = stopMapper.toEntity(request);

        assertNotNull(stop.getRoute());
        Assertions.assertEquals(routeId, stop.getRoute().getId());
    }

    @Test
    @DisplayName("Debe preservar todos los campos durante el ciclo completo de mapeo")
    void shouldPreserveAllFieldsDuringFullCycleMapping() {

        StopCreateRequest request = new StopCreateRequest(
                "Complete Stop",
                5,
                new BigDecimal("10.1234567"),
                new BigDecimal("-75.9876543"),
                100L
        );

        Stop stop = stopMapper.toEntity(request);
        stop.setId(999L);
        stop.setRoute(Route.builder()
                .id(100L)
                .code("TEST")
                .name("Test Route")
                .build());

        StopResponse response = stopMapper.toResponse(stop);

        Assertions.assertEquals(999L, response.id());
        Assertions.assertEquals("Complete Stop", response.name());
        Assertions.assertEquals(5, response.order());
        Assertions.assertEquals(new BigDecimal("10.1234567"), response.lat());
        Assertions.assertEquals(new BigDecimal("-75.9876543"), response.lng());
        Assertions.assertEquals(100L, response.routeId());
        Assertions.assertEquals("Test Route", response.routeName());
        Assertions.assertEquals("TEST", response.routeCode());
    }
}