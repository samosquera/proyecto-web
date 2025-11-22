package com.bers.services.service;

import com.bers.api.dtos.FareRuleDtos.FareRuleCreateRequest;
import com.bers.api.dtos.FareRuleDtos.FareRuleResponse;
import com.bers.domain.entities.FareRule;
import com.bers.domain.entities.Route;
import com.bers.domain.entities.Stop;
import com.bers.domain.entities.enums.DynamicPricingStatus;
import com.bers.domain.repositories.FareRuleRepository;
import com.bers.domain.repositories.RouteRepository;
import com.bers.domain.repositories.StopRepository;
import com.bers.services.mappers.FareRuleMapper;
import com.bers.services.service.serviceImple.FareRuleServiceImpl;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FareRuleService test")
class FareRuleServiceImplTest {
    @Mock
    private FareRuleRepository fareRuleRepository;
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private StopRepository stopRepository;
    @Spy
    private FareRuleMapper fareRuleMapper = Mappers.getMapper(FareRuleMapper.class);
    @InjectMocks
    private FareRuleServiceImpl fareRuleService;
    private FareRule fareRule;
    private Route route;
    private Stop fromStop;
    private Stop toStop;
    private FareRuleCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        route = Route.builder().id(1L).build();
        fromStop = Stop.builder().id(1L).name("A").order(0).build();
        toStop = Stop.builder().id(2L).name("B").order(1).build();
        Map<String, Object> discounts = new HashMap<>();
        discounts.put("student", 0.15);
        fareRule = FareRule.builder()
                .id(1L)
                .basePrice(new BigDecimal("50000"))
                .discounts(discounts)
                .dynamicPricing(DynamicPricingStatus.ON)
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .build();
        createRequest = new FareRuleCreateRequest(
                new BigDecimal("50000"), discounts,
                DynamicPricingStatus.ON, 1L, 1L, 2L
        );
    }

    @Test
    @DisplayName("Debe crear farerule")
    void shouldCreateFareRuleSuccessfully() {
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
        when(fareRuleRepository.save(any())).thenReturn(fareRule);

        FareRuleResponse result = fareRuleService.createFareRule(createRequest);

        assertNotNull(result);
        Assertions.assertEquals(new BigDecimal("50000"), result.basePrice());
        verify(fareRuleRepository).save(any(FareRule.class));
        verify(fareRuleMapper).toEntity(createRequest);
        verify(fareRuleMapper).toResponse(any(FareRule.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción con stop sequence inválida")
    void shouldThrowExceptionWithInvalidStopSequence() {
        Stop invalidStop = Stop.builder().id(3L).order(2).build();
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(invalidStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fareRuleService.createFareRule(createRequest)
        );

        Assertions.assertTrue(exception.getMessage().contains("Invalid stop sequence"));
        verify(fareRuleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe calcular dynamic price con high occupation")
    void shouldCalculateDynamicPriceWithHighOccupation() {
        when(fareRuleRepository.findById(1L)).thenReturn(Optional.of(fareRule));

        BigDecimal result = fareRuleService.calculateDynamicPrice(1L, 0.85);

        assertNotNull(result);
        Assertions.assertTrue(result.compareTo(new BigDecimal("50000")) > 0);
        Assertions.assertEquals(new BigDecimal("65000.00"), result);
    }

    @Test
    @DisplayName("Debe calcular dynamic price con low occupation")
    void shouldCalculateDynamicPriceWithLowOccupation() {
        when(fareRuleRepository.findById(1L)).thenReturn(Optional.of(fareRule));

        BigDecimal result = fareRuleService.calculateDynamicPrice(1L, 0.25);

        assertNotNull(result);
        Assertions.assertTrue(result.compareTo(new BigDecimal("50000")) < 0);
        Assertions.assertEquals(new BigDecimal("42500.00"), result);
    }

    @Test
    @DisplayName("Debe retornar base price cuando dynamic pricing está OFF")
    void shouldReturnBasePriceWhenPricingOff() {
        fareRule.setDynamicPricing(DynamicPricingStatus.OFF);
        when(fareRuleRepository.findById(1L)).thenReturn(Optional.of(fareRule));

        BigDecimal result = fareRuleService.calculateDynamicPrice(1L, 0.9);

        Assertions.assertEquals(new BigDecimal("50000"), result);
    }

    @Test
    @DisplayName("Debe obtener fareRules con dynamic pricing")
    void shouldGetDynamicPricingRules() {
        List<FareRule> rules = Arrays.asList(fareRule);
        when(routeRepository.existsById(1L)).thenReturn(true);
        when(fareRuleRepository.findDynamicPricingRules(1L)).thenReturn(rules);

        List<FareRuleResponse> result = fareRuleService.getDynamicPricingRules(1L);

        assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        verify(fareRuleMapper, times(1)).toResponse(any(FareRule.class));
    }
}