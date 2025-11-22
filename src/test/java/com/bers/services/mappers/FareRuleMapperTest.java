package com.bers.services.mappers;

import com.bers.api.dtos.FareRuleDtos.FareRuleCreateRequest;
import com.bers.api.dtos.FareRuleDtos.FareRuleResponse;
import com.bers.domain.entities.FareRule;
import com.bers.domain.entities.Route;
import com.bers.domain.entities.Stop;
import com.bers.domain.entities.enums.DynamicPricingStatus;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("FareRuleMapper Tests")
class FareRuleMapperTest {
    private FareRuleMapper fareRuleMapper;
    @BeforeEach
    void setUp() {
        fareRuleMapper = Mappers.getMapper(FareRuleMapper.class);
    }

    @Test
    @DisplayName("Debe mapear FareRuleCreateRequest a la entidad FareRule")
    void shouldMapCreateRequestToEntity() {
        Map<String, Object> discounts = new HashMap<>();
        discounts.put("student", 0.15);
        discounts.put("senior", 0.20);

        FareRuleCreateRequest request = new FareRuleCreateRequest(
                new BigDecimal("50000"),
                discounts,
                DynamicPricingStatus.ON,
                1L,
                2L,
                3L
        );

        FareRule fareRule = fareRuleMapper.toEntity(request);

        assertNotNull(fareRule);
        Assertions.assertEquals(new BigDecimal("50000"), fareRule.getBasePrice());
        Assertions.assertEquals(DynamicPricingStatus.ON, fareRule.getDynamicPricing());
        assertNotNull(fareRule.getDiscounts());
        Assertions.assertEquals(2, fareRule.getDiscounts().size());
    }

    @Test
    @DisplayName("Debe mapear la entidad FareRule a FareRuleResponse")
    void shouldMapEntityToResponse() {
        Route route = Route.builder().id(1L).build();
        Stop fromStop = Stop.builder().id(2L).name("Bogotá").build();
        Stop toStop = Stop.builder().id(3L).name("Tunja").build();

        Map<String, Object> discounts = new HashMap<>();
        discounts.put("child", 0.25);

        FareRule fareRule = FareRule.builder()
                .id(1L)
                .basePrice(new BigDecimal("45000"))
                .discounts(discounts)
                .dynamicPricing(DynamicPricingStatus.OFF)
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .build();

        FareRuleResponse response = fareRuleMapper.toResponse(fareRule);

        assertNotNull(response);
        Assertions.assertEquals(1L, response.id());
        Assertions.assertEquals(new BigDecimal("45000"), response.basePrice());
        Assertions.assertEquals("OFF", response.dynamicPricing());
        Assertions.assertEquals(1L, response.routeId());
        Assertions.assertEquals(2L, response.fromStopId());
        Assertions.assertEquals("Bogotá", response.fromStopName());
        Assertions.assertEquals(3L, response.toStopId());
        Assertions.assertEquals("Tunja", response.toStopName());
    }
}