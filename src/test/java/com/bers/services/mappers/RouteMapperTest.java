package com.bers.services.mappers;

import com.bers.api.dtos.RouteDtos.*;
import com.bers.domain.entities.Route;
import com.bers.domain.entities.Stop;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assertions.*;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RouteMapper Tests")
class RouteMapperTest {
    private RouteMapper routeMapper;

    @BeforeEach
    void setUp() {
        routeMapper = Mappers.getMapper(RouteMapper.class);
    }

    @Test
    @DisplayName("Debe mapear RouteCreateRequest a la entidad Route")
    void shouldMapCreateRequestToEntity() {

        RouteCreateRequest request = new RouteCreateRequest(
                "BOG-TUN",
                "Bogotá - Tunja",
                "Bogotá",
                "Tunja",
                150,
                180
        );

        Route route = routeMapper.toEntity(request);

        assertNotNull(route);
        Assertions.assertEquals("BOG-TUN", route.getCode());
        Assertions.assertEquals("Bogotá - Tunja", route.getName());
        Assertions.assertEquals("Bogotá", route.getOrigin());
        Assertions.assertEquals("Tunja", route.getDestination());
        Assertions.assertEquals(150, route.getDistanceKm());
        Assertions.assertEquals(180, route.getDurationMin());
        assertNull(route.getId());
    }

    @Test
    @DisplayName("Debe actualizar la entidad Route")
    void shouldUpdateEntityFromUpdateRequest() {

        Route existingRoute = Route.builder()
                .id(1L)
                .code("BOG-TUN")
                .name("Old Name")
                .origin("Bogotá")
                .destination("Tunja")
                .distanceKm(150)
                .durationMin(180)
                .build();

        RouteUpdateRequest request = new RouteUpdateRequest(
                "Bogotá - Tunja Express",
                160,
                170
        );


        routeMapper.updateEntity(request, existingRoute);

        Assertions.assertEquals("Bogotá - Tunja Express", existingRoute.getName());
        Assertions.assertEquals(160, existingRoute.getDistanceKm());
        Assertions.assertEquals(170, existingRoute.getDurationMin());

        Assertions.assertEquals("BOG-TUN", existingRoute.getCode());
        Assertions.assertEquals("Bogotá", existingRoute.getOrigin());
        Assertions.assertEquals("Tunja", existingRoute.getDestination());
    }

    @Test
    @DisplayName("Debe mapear la entidad Route a RouteResponse")
    void shouldMapEntityToResponse() {

        Route route = Route.builder()
                .id(1L)
                .code("BOG-TUN")
                .name("Bogotá - Tunja")
                .origin("Bogotá")
                .destination("Tunja")
                .distanceKm(150)
                .durationMin(180)
                .stops(new ArrayList<>())
                .build();

        RouteResponse response = routeMapper.toResponse(route);

        assertNotNull(response);
        Assertions.assertEquals(1L, response.id());
        Assertions.assertEquals("BOG-TUN", response.code());
        Assertions.assertEquals("Bogotá - Tunja", response.name());
        Assertions.assertEquals("Bogotá", response.origin());
        Assertions.assertEquals("Tunja", response.destination());
        Assertions.assertEquals(150, response.distanceKm());
        Assertions.assertEquals(180, response.durationMin());
        assertNotNull(response.stops());
        Assertions.assertTrue(response.stops().isEmpty());
    }

    @Test
    @DisplayName("Debe mapear Route con paradas a RouteResponse con StopSummary")
    void shouldMapRouteWithStopsToResponse() {
        Route route = Route.builder()
                .id(1L)
                .code("BOG-TUN")
                .name("Bogotá - Tunja")
                .origin("Bogotá")
                .destination("Tunja")
                .distanceKm(150)
                .durationMin(180)
                .build();

        List<Stop> stops = new ArrayList<>();
        stops.add(Stop.builder()
                .id(1L)
                .name("Terminal Bogotá")
                .order(0)
                .lat(new BigDecimal("4.6533"))
                .lng(new BigDecimal("-74.0836"))
                .route(route)
                .build());
        stops.add(Stop.builder()
                .id(2L)
                .name("Zipaquirá")
                .order(1)
                .lat(new BigDecimal("5.0208"))
                .lng(new BigDecimal("-73.9949"))
                .route(route)
                .build());
        stops.add(Stop.builder()
                .id(3L)
                .name("Terminal Tunja")
                .order(2)
                .lat(new BigDecimal("5.5353"))
                .lng(new BigDecimal("-73.3678"))
                .route(route)
                .build());

        route.setStops(stops);


        RouteResponse response = routeMapper.toResponse(route);


        assertNotNull(response);
        assertNotNull(response.stops());
        Assertions.assertEquals(3, response.stops().size());

        StopSummary firstStop = response.stops().getFirst();
        Assertions.assertEquals(1L, firstStop.id());
        Assertions.assertEquals("Terminal Bogotá", firstStop.name());
        Assertions.assertEquals(0, firstStop.order());
        Assertions.assertEquals(new BigDecimal("4.6533"), firstStop.lat());
        Assertions.assertEquals(new BigDecimal("-74.0836"), firstStop.lng());

        StopSummary secondStop = response.stops().get(1);
        Assertions.assertEquals(2L, secondStop.id());
        Assertions.assertEquals("Zipaquirá", secondStop.name());
        Assertions.assertEquals(1, secondStop.order());

        StopSummary thirdStop = response.stops().get(2);
        Assertions.assertEquals(3L, thirdStop.id());
        Assertions.assertEquals("Terminal Tunja", thirdStop.name());
        Assertions.assertEquals(2, thirdStop.order());
    }

    @Test
    @DisplayName("Debe manejar la lista de paradas nula (null stops list)")
    void shouldHandleNullStopsList() {

        Route route = Route.builder()
                .id(1L)
                .code("BOG-TUN")
                .name("Bogotá - Tunja")
                .origin("Bogotá")
                .destination("Tunja")
                .distanceKm(150)
                .durationMin(180)
                .stops(null)
                .build();

        RouteResponse response = routeMapper.toResponse(route);

        assertNotNull(response);
        assertNull(response.stops());
    }

    @Test
    @DisplayName("Debe preservar todos los campos durante el mapeo")
    void shouldPreserveAllFieldsDuringMapping() {
        RouteCreateRequest request = new RouteCreateRequest(
                "MED-CAL",
                "Medellín - Cali",
                "Medellín",
                "Cali",
                420,
                540
        );

        Route route = routeMapper.toEntity(request);
        route.setId(5L);
        RouteResponse response = routeMapper.toResponse(route);

        Assertions.assertEquals(5L, response.id());
        Assertions.assertEquals(request.code(), response.code());
        Assertions.assertEquals(request.name(), response.name());
        Assertions.assertEquals(request.origin(), response.origin());
        Assertions.assertEquals(request.destination(), response.destination());
        Assertions.assertEquals(request.distanceKm(), response.distanceKm());
        Assertions.assertEquals(request.durationMin(), response.durationMin());
    }

    @Test
    @DisplayName("Debe manejar valores mínimos")
    void shouldHandleMinimumValues() {

        RouteCreateRequest request = new RouteCreateRequest(
                "A",
                "B",
                "C",
                "D",
                1,
                1
        );

        Route route = routeMapper.toEntity(request);

        assertNotNull(route);
        Assertions.assertEquals("A", route.getCode());
        Assertions.assertEquals(1, route.getDistanceKm());
        Assertions.assertEquals(1, route.getDurationMin());
    }
}