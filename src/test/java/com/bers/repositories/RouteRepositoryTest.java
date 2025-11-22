package com.bers.repositories;

import com.bers.AbstractRepositoryTest;
import com.bers.domain.entities.Route;
import com.bers.domain.entities.Stop;
import com.bers.domain.repositories.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
class RouteRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private RouteRepository routeRepository;

    @Test
    @DisplayName("Guardar una route")
    void shouldSaveRoute() {
        var route = createRoute("BOG-MED",
                "Bogotá - Medellín","Bogotá",
                "Medellín",400,480);
        Route saved = routeRepository.save(route);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("BOG-MED");
    }

    @Test
    @DisplayName("Buscar route por code")
    void shouldFindRouteByCode() {
        var route =  createRoute("BOG-CAL","Bogotá - Cali",
                "Bogotá","Cali",450,500
                );

        routeRepository.save(route);
        Optional<Route> found = routeRepository.findByCode("BOG-CAL");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Bogotá - Cali");
    }

    @Test
    @DisplayName("Buscar route por origin y destination")
    void shouldFindRoutesByOriginAndDestination() {
        var route1 = createRoute("BOG-MED-1",
                "Bogotá - Medellín Express","Bogotá",
                "Medellín",400,420
                );

        var route2 = createRoute("BOG-MED-2",
                "Bogotá - Medellín Normal","Bogotá",
                "Medellín",400,480
        );

        var route3 =  createRoute("CAL-BAR","Cali - Barranquilla",
                "Cali","Barranquilla",900,720
                );

        routeRepository.saveAll(List.of(route1, route2, route3));
        List<Route> routes = routeRepository.findByOriginIgnoreCaseAndDestinationIgnoreCase("Bogotá", "Medellín");

        // Then
        assertThat(routes).hasSize(2);
        assertThat(routes).extracting(Route::getOrigin).containsOnly("Bogotá");
    }

    @Test
    @DisplayName("Buscar route por origi o destination ignorando mayus")
    void shouldFindRoutesByOriginOrDestinationIgnoringCase() {
        var route1 = createRoute("BOG-MED","Bogotá - Medellín",
                "Bogotá","Medellín",400,480
                );


        var route2 = createRoute("MED-CAL","Medellín - Cali",
                "Medellín","Cali",300,360
                );
        routeRepository.saveAll(List.of(route1, route2));

        List<Route> routes = routeRepository
                .findByOriginContainingIgnoreCaseOrDestinationContainingIgnoreCase(
                        "medellín", "medellín");

        assertThat(routes).hasSize(2);
    }

    @Test
    @DisplayName("Buscar una route por id con stops")
    void shouldFindRouteByIdWithStops() {
        var route = createRoute("BOG-TUN-MED","Bogotá - Tunja - Medellín",
                "Bogotá", "Medellín",450,540
                );

        var stop1 = createStop(route,"Bogotá Terminal",
                1,new BigDecimal("4.7110"),new BigDecimal("-74.0721")
                );

        var stop2 = createStop(route,"Tunja",2,
                new BigDecimal("5.5353"),new BigDecimal("-73.3678")
        );

        var stop3 = createStop(route,"Medellín Terminal",3,
                new BigDecimal("6.2442"),new BigDecimal("-75.5812")
                );

        route.getStops().addAll(List.of(stop1, stop2, stop3));
        Route saved = routeRepository.save(route);

        Optional<Route> found = routeRepository.findByIdWithStops(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStops()).hasSize(3);
        assertThat(found.get().getStops().get(0).getName()).isEqualTo("Bogotá Terminal");
        assertThat(found.get().getStops().get(1).getName()).isEqualTo("Tunja");
    }

    @Test
    @DisplayName("Buscar si el code de route existe")
    void shouldCheckIfCodeExists() {
        var route = createRoute("EXIST-CODE","Test Route",
                "Origin", "Destination",100,
                120
                );
        routeRepository.save(route);

        boolean exists = routeRepository.existsByCode("EXIST-CODE");
        boolean notExists = routeRepository.existsByCode("NO-EXIST");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    private Route createRoute(String code, String name, String origin, String destination, Integer distanceKm,  Integer durationMin) {
        return Route.builder()
                .code(code)
                .name(name)
                .origin(origin)
                .destination(destination)
                .distanceKm(distanceKm)
                .durationMin(durationMin)
                .build();
    }
    private Stop createStop(Route rute, String stopName,Integer order,BigDecimal lat,BigDecimal lng) {
        return Stop.builder()
                .route(rute)
                .name(stopName)
                .order(order)
                .lat(lat)
                .lng(lng)
                .build();
    }
}