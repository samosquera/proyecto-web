package com.bers.repositories;

import com.bers.AbstractRepositoryTest;
import com.bers.domain.entities.FareRule;
import com.bers.domain.entities.Route;
import com.bers.domain.entities.Stop;
import com.bers.domain.entities.enums.DynamicPricingStatus;
import com.bers.domain.repositories.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class FareRuleRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private FareRuleRepository fareRuleRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private StopRepository stopRepository;

    @Test
    @DisplayName("Guardar fareRule")
    void shouldSaveFareRule() {
        var route = createAndSaveRoute();
        var fromStop = createAndSaveStop(route, "Bogotá", 1);
        var toStop = createAndSaveStop(route, "Medellín", 2);

        var fareRule = createFareRule(route,fromStop,toStop, "50000");

        Map<String, Object> discounts = new HashMap<>();
        discounts.put("student", 0.15);
        discounts.put("senior", 0.20);
        fareRule.setDiscounts(discounts);

        FareRule saved = fareRuleRepository.save(fareRule);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBasePrice()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(saved.getDiscounts()).containsKeys("student", "senior");
    }

    @Test
    @DisplayName("Buscar fareRule por id de route")
    void shouldFindFareRulesByRouteId() {
        Route route = createAndSaveRoute();
        Stop stop1 = createAndSaveStop(route, "Stop 1", 1);
        Stop stop2 = createAndSaveStop(route, "Stop 2", 2);
        Stop stop3 = createAndSaveStop(route, "Stop 3", 3);

        fareRuleRepository.saveAll(List.of(
                createFareRule(route, stop1, stop2, "40000"),
                createFareRule(route, stop2, stop3, "30000"),
                createFareRule(route, stop1, stop3, "60000")
        ));

        List<FareRule> fareRules = fareRuleRepository.findByRouteId(route.getId());

        assertThat(fareRules).hasSize(3);
        assertThat(fareRules).allMatch(fr -> fr.getRoute().getId().equals(route.getId()));
    }

    @Test
    @DisplayName("Buscar fareRule por route y stop")
    void shouldFindFareRuleByRouteIdAndFromStopIdAndToStopId() {
        Route route = createAndSaveRoute();
        Stop fromStop = createAndSaveStop(route, "Origin", 1);
        Stop toStop = createAndSaveStop(route, "Destination", 2);

        FareRule fareRule = createFareRule(route, fromStop, toStop, "45000");
        fareRuleRepository.save(fareRule);
        Optional<FareRule> found = fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(
                route.getId(), fromStop.getId(), toStop.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getBasePrice()).isEqualByComparingTo(new BigDecimal("45000"));
    }

    @Test
    @DisplayName("Buscar fareRule que NO existe")
    void shouldNotFindFareRuleForNonExistentSegment() {
        Route route = createAndSaveRoute();
        Stop stop1 = createAndSaveStop(route, "Stop A", 1);
        Stop stop2 = createAndSaveStop(route, "Stop B", 2);
        Stop stop3 = createAndSaveStop(route, "Stop C", 3);

        fareRuleRepository.save(createFareRule(route, stop1, stop2, "30000"));

        Optional<FareRule> found = fareRuleRepository.findByRouteIdAndFromStopIdAndToStopId(
                route.getId(), stop2.getId(), stop3.getId());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Buscar fareRule por query personalizada")
    void shouldFindFareForSegmentUsingCustomQuery() {
        Route route = createAndSaveRoute();
        Stop fromStop = createAndSaveStop(route, "Start", 1);
        Stop toStop = createAndSaveStop(route, "End", 2);

        FareRule fareRule = createFareRule(route, fromStop, toStop, "55000");
        fareRuleRepository.save(fareRule);

        Optional<FareRule> found = fareRuleRepository.findFareForSegment(
                route.getId(), fromStop.getId(), toStop.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getBasePrice()).isEqualByComparingTo(new BigDecimal("55000"));
        assertThat(found.get().getFromStop().getName()).isEqualTo("Start");
        assertThat(found.get().getToStop().getName()).isEqualTo("End");
    }

    @Test
    @DisplayName("Buscar fareRule de precio dinamico ON")
    void shouldFindDynamicPricingRules() {
        // Given
        Route route = createAndSaveRoute();
        Stop stop1 = createAndSaveStop(route, "A", 1);
        Stop stop2 = createAndSaveStop(route, "B", 2);
        Stop stop3 = createAndSaveStop(route, "C", 3);

        FareRule dynamicRule1 = createFareRule(route, stop1, stop2, "40000");
        dynamicRule1.setDynamicPricing(DynamicPricingStatus.ON);

        FareRule staticRule = createFareRule(route, stop2, stop3, "35000");
        staticRule.setDynamicPricing(DynamicPricingStatus.OFF);

        FareRule dynamicRule2 = createFareRule(route, stop1, stop3, "70000");
        dynamicRule2.setDynamicPricing(DynamicPricingStatus.ON);

        fareRuleRepository.saveAll(List.of(dynamicRule1, staticRule, dynamicRule2));

        List<FareRule> dynamicRules = fareRuleRepository.findDynamicPricingRules(route.getId());

        assertThat(dynamicRules).hasSize(2);
        assertThat(dynamicRules).allMatch(fr ->
                fr.getDynamicPricing() == DynamicPricingStatus.ON);
    }

    @Test
    @DisplayName("Manejar la persistencia de descuentos")
    void shouldHandleDiscountsJsonb() {
        var route = createAndSaveRoute();
        var fromStop = createAndSaveStop(route, "From", 1);
        var toStop = createAndSaveStop(route, "To", 2);

        var fareRule = createFareRule(route, fromStop, toStop, "55000");

        Map<String, Object> discounts = new HashMap<>();
        discounts.put("student", 0.15);
        discounts.put("senior", 0.20);
        discounts.put("child", 0.50);
        discounts.put("disabled", 0.25);
        fareRule.setDiscounts(discounts);
        FareRule saved = fareRuleRepository.save(fareRule);
        FareRule found = fareRuleRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getDiscounts()).hasSize(4);
        assertThat(found.getDiscounts().get("student")).isEqualTo(0.15);
        assertThat(found.getDiscounts().get("senior")).isEqualTo(0.20);
        assertThat(found.getDiscounts().get("child")).isEqualTo(0.50);
        assertThat(found.getDiscounts().get("disabled")).isEqualTo(0.25);
    }

    @Test
    @DisplayName("Multiple segmento route")
    void shouldHandleMultipleSegmentsInSameRoute() {
        Route route = createAndSaveRoute();
        Stop stop1 = createAndSaveStop(route, "Bogotá", 1);
        Stop stop2 = createAndSaveStop(route, "Tunja", 2);
        Stop stop3 = createAndSaveStop(route, "Bucaramanga", 3);
        Stop stop4 = createAndSaveStop(route, "Cúcuta", 4);

        fareRuleRepository.save(createFareRule(route, stop1, stop2, "25000"));
        fareRuleRepository.save(createFareRule(route, stop2, stop3, "30000"));
        fareRuleRepository.save(createFareRule(route, stop3, stop4, "20000"));

        fareRuleRepository.save(createFareRule(route, stop1, stop3, "50000"));
        fareRuleRepository.save(createFareRule(route, stop1, stop4, "65000"));

        List<FareRule> allRules = fareRuleRepository.findByRouteId(route.getId());
        Optional<FareRule> longDistance = fareRuleRepository.findFareForSegment(
                route.getId(), stop1.getId(), stop4.getId());

        assertThat(allRules).hasSize(5);
        assertThat(longDistance).isPresent();
        assertThat(longDistance.get().getBasePrice()).isEqualByComparingTo(new BigDecimal("65000"));
    }

    @Test
    @DisplayName("Actualizar precio base y estado dinamico")
    void shouldUpdateFareRule() {
        Route route = createAndSaveRoute();
        Stop fromStop = createAndSaveStop(route, "X", 1);
        Stop toStop = createAndSaveStop(route, "Y", 2);

        FareRule fareRule = createFareRule(route, fromStop, toStop, "40000");
        FareRule saved = fareRuleRepository.save(fareRule);

        saved.setBasePrice(new BigDecimal("45000"));
        saved.setDynamicPricing(DynamicPricingStatus.ON);
        fareRuleRepository.save(saved);

        FareRule updated = fareRuleRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getBasePrice()).isEqualByComparingTo(new BigDecimal("45000"));
        assertThat(updated.getDynamicPricing()).isEqualTo(DynamicPricingStatus.ON);
    }

    @Test
    @DisplayName("Manejar y persistir descuentos vacios")
    void shouldHandleEmptyDiscounts() {
        Route route = createAndSaveRoute();
        Stop fromStop = createAndSaveStop(route, "P", 1);
        Stop toStop = createAndSaveStop(route, "Q", 2);

        FareRule fareRule = createFareRule(route, fromStop, toStop, "30000");
        fareRule.setDiscounts(new HashMap<>()); // Empty discounts

        FareRule saved = fareRuleRepository.save(fareRule);
        FareRule found = fareRuleRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getDiscounts()).isEmpty();
    }
    private Route createAndSaveRoute() {
        Route route = Route.builder()
                .code("ROUTE-554")
                .name("Test Route")
                .origin("Origin")
                .destination("Destination")
                .distanceKm(400)
                .durationMin(480)
                .build();
        return routeRepository.save(route);
    }

    private Stop createAndSaveStop(Route route, String name, Integer order) {
        Stop stop = Stop.builder()
                .route(route)
                .name(name)
                .order(order)
                .lat(new BigDecimal("4.7110"))
                .lng(new BigDecimal("-74.0721"))
                .build();
        return stopRepository.save(stop);
    }

    private FareRule createFareRule(Route route, Stop fromStop, Stop toStop, String price) {
        return FareRule.builder()
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .basePrice(new BigDecimal(price))
                .dynamicPricing(DynamicPricingStatus.OFF)
                .discounts(new HashMap<>())
                .build();
    }
}