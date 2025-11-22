package com.bers.services.service.serviceImple;

import com.bers.api.dtos.FareRuleDtos.FareRuleCreateRequest;
import com.bers.api.dtos.FareRuleDtos.FareRuleResponse;
import com.bers.api.dtos.FareRuleDtos.FareRuleUpdateRequest;
import com.bers.domain.entities.FareRule;
import com.bers.domain.entities.Route;
import com.bers.domain.entities.Stop;
import com.bers.domain.entities.enums.DynamicPricingStatus;
import com.bers.domain.repositories.FareRuleRepository;
import com.bers.domain.repositories.RouteRepository;
import com.bers.domain.repositories.StopRepository;
import com.bers.services.mappers.FareRuleMapper;
import com.bers.services.service.FareRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FareRuleServiceImpl implements FareRuleService {

    private final FareRuleRepository fareRuleRepository;
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final FareRuleMapper fareRuleMapper;

    @Override
    public FareRuleResponse createFareRule(FareRuleCreateRequest request) {
        Route route = routeRepository.findById(request.routeId())
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + request.routeId()));

        Stop fromStop = stopRepository.findById(request.fromStopId())
                .orElseThrow(() -> new IllegalArgumentException("From stop not found: " + request.fromStopId()));

        var toStop = stopRepository.findById(request.toStopId())
                .orElseThrow(() -> new IllegalArgumentException("To stop not found: " + request.toStopId()));

        if (fromStop.getOrder() >= toStop.getOrder()) {
            throw new IllegalArgumentException("Invalid stop sequence");
        }

        FareRule fareRule = fareRuleMapper.toEntity(request);
        fareRule.setRoute(route);
        fareRule.setFromStop(fromStop);
        fareRule.setToStop(toStop);

        FareRule savedFareRule = fareRuleRepository.save(fareRule);
        return fareRuleMapper.toResponse(savedFareRule);
    }

    @Override
    public FareRuleResponse updateFareRule(Long id, FareRuleUpdateRequest request) {
        FareRule fareRule = fareRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FareRule not found: " + id));

        fareRuleMapper.updateEntity(request, fareRule);
        FareRule updatedFareRule = fareRuleRepository.save(fareRule);
        return fareRuleMapper.toResponse(updatedFareRule);
    }

    @Override
    @Transactional
    public FareRuleResponse getFareRuleById(Long id) {
        FareRule fareRule = fareRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FareRule not found: " + id));
        return fareRuleMapper.toResponse(fareRule);
    }

    @Override
    @Transactional
    public List<FareRuleResponse> getAllFareRules() {
        return fareRuleRepository.findAll().stream()
                .map(fareRuleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<FareRuleResponse> getFareRulesByRouteId(Long routeId) {
        if (!routeRepository.existsById(routeId)) {
            throw new IllegalArgumentException("Route not found: " + routeId);
        }
        return fareRuleRepository.findByRouteId(routeId).stream()
                .map(fareRuleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FareRuleResponse getFareRuleForSegment(Long routeId, Long fromStopId, Long toStopId) {
        FareRule fareRule = fareRuleRepository.findFareForSegment(routeId, fromStopId, toStopId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "FareRule not found for segment: route=" + routeId +
                                ", from=" + fromStopId + ", to=" + toStopId));
        return fareRuleMapper.toResponse(fareRule);
    }

    @Override
    @Transactional
    public List<FareRuleResponse> getDynamicPricingRules(Long routeId) {
        if (!routeRepository.existsById(routeId)) {
            throw new IllegalArgumentException("Route not found: " + routeId);
        }
        return fareRuleRepository.findDynamicPricingRules(routeId).stream()
                .map(fareRuleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteFareRule(Long id) {
        if (!fareRuleRepository.existsById(id)) {
            throw new IllegalArgumentException("FareRule not found: " + id);
        }
        fareRuleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public BigDecimal calculateDynamicPrice(Long fareRuleId, Double occupancyRate) {
        FareRule fareRule = fareRuleRepository.findById(fareRuleId)
                .orElseThrow(() -> new IllegalArgumentException("FareRule not found: " + fareRuleId));

        if (fareRule.getDynamicPricing() == DynamicPricingStatus.OFF) {
            return fareRule.getBasePrice();
        }

        BigDecimal basePrice = fareRule.getBasePrice();
        BigDecimal multiplier = BigDecimal.ONE;

        // Ajuste según tasa de ocupación
        if (occupancyRate > 0.8) {
            multiplier = new BigDecimal("1.3"); // +30%
        } else if (occupancyRate > 0.6) {
            multiplier = new BigDecimal("1.15"); // +15%
        } else if (occupancyRate < 0.3) {
            multiplier = new BigDecimal("0.85"); // -15%
        }

        return basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
