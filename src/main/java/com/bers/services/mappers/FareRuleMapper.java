package com.bers.services.mappers;

import com.bers.api.dtos.FareRuleDtos.FareRuleCreateRequest;
import com.bers.api.dtos.FareRuleDtos.FareRuleResponse;
import com.bers.api.dtos.FareRuleDtos.FareRuleUpdateRequest;
import com.bers.domain.entities.FareRule;
import com.bers.domain.entities.Route;
import com.bers.domain.entities.Stop;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FareRuleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passengerDiscounts", ignore = true)
    @Mapping(target = "basePrice", source = "basePrice")
    @Mapping(target = "discounts", source = "discounts")
    @Mapping(target = "dynamicPricing", source = "dynamicPricing")
    @Mapping(target = "route", source = "routeId", qualifiedByName = "mapRoute")
    @Mapping(target = "fromStop", source = "fromStopId", qualifiedByName = "mapStop")
    @Mapping(target = "toStop", source = "toStopId", qualifiedByName = "mapStop")
    FareRule toEntity(FareRuleCreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passengerDiscounts", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "fromStop", ignore = true)
    @Mapping(target = "toStop", ignore = true)
    @Mapping(target = "basePrice", source = "basePrice")
    @Mapping(target = "discounts", source = "discounts")
    @Mapping(target = "dynamicPricing", source = "dynamicPricing")
    void updateEntity(FareRuleUpdateRequest dto, @MappingTarget FareRule entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "basePrice", source = "basePrice")
    @Mapping(target = "discounts", source = "discounts")
    @Mapping(target = "dynamicPricing", source = "dynamicPricing")
    @Mapping(target = "routeId", source = "route.id")
    @Mapping(target = "fromStopId", source = "fromStop.id")
    @Mapping(target = "toStopId", source = "toStop.id")
    @Mapping(target = "fromStopName", source = "fromStop.name")
    @Mapping(target = "toStopName", source = "toStop.name")
    FareRuleResponse toResponse(FareRule entity);

    @Named("mapRoute")
    default Route mapRoute(Long id) {
        if (id == null) return null;
        Route r = new Route();
        r.setId(id);
        return r;
    }

    @Named("mapStop")
    default Stop mapStop(Long id) {
        if (id == null) return null;
        Stop s = new Stop();
        s.setId(id);
        return s;
    }
}