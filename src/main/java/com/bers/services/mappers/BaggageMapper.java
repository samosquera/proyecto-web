package com.bers.services.mappers;

import com.bers.api.dtos.BaggageDtos.BaggageCreateRequest;
import com.bers.api.dtos.BaggageDtos.BaggageResponse;
import com.bers.api.dtos.BaggageDtos.BaggageUpdateRequest;
import com.bers.domain.entities.Baggage;
import com.bers.domain.entities.Ticket;
import com.bers.domain.entities.Trip;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BaggageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "weightKg", source = "weightKg")
    @Mapping(target = "fee", source = "weightKg", qualifiedByName = "calculateFee")
    @Mapping(target = "tagCode", expression = "java(generateTagCode())")
    @Mapping(target = "ticket", source = "ticketId", qualifiedByName = "mapTicket")
    Baggage toEntity(BaggageCreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "weightKg", ignore = true)
    @Mapping(target = "tagCode", ignore = true)
    @Mapping(target = "ticket", ignore = true)
    @Mapping(target = "fee", source = "fee")
    void updateEntity(BaggageUpdateRequest dto, @MappingTarget Baggage baggage);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "weightKg", source = "weightKg")
    @Mapping(target = "fee", source = "fee")
    @Mapping(target = "tagCode", source = "tagCode")
    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "passengerName", source = "ticket.passenger.username")
    @Mapping(target = "tripInfo", source = "ticket.trip", qualifiedByName = "formatTripInfo")
    @Mapping(target = "excessWeight", source = "weightKg", qualifiedByName = "isExcessWeight")
    BaggageResponse toResponse(Baggage entity);

    @Named("mapTicket")
    default Ticket mapTicket(Long id) {
        if (id == null) return null;
        Ticket t = new Ticket();
        t.setId(id);
        return t;
    }

    @Named("formatTripInfo")
    default String formatTripInfo(Trip trip) {
        if (trip == null || trip.getRoute() == null) return null;
        return trip.getRoute().getOrigin() + " → " + trip.getRoute().getDestination() +
                " (" + trip.getDate() + ")";
    }

    @Named("calculateFee")
    default java.math.BigDecimal calculateFee(java.math.BigDecimal weightKg) {
        if (weightKg == null) return java.math.BigDecimal.ZERO;
        java.math.BigDecimal freeWeight = new java.math.BigDecimal("20.0");
        java.math.BigDecimal pricePerKg = new java.math.BigDecimal("2000");

        if (weightKg.compareTo(freeWeight) <= 0) {
            return java.math.BigDecimal.ZERO;
        }

        java.math.BigDecimal excess = weightKg.subtract(freeWeight);
        return excess.multiply(pricePerKg);
    }

    @Named("isExcessWeight")
    default Boolean isExcessWeight(java.math.BigDecimal weightKg) {
        if (weightKg == null) return false;
        java.math.BigDecimal freeWeight = new java.math.BigDecimal("20.0");
        return weightKg.compareTo(freeWeight) > 0;
    }

    default String generateTagCode() {
        return "BAG-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
    }
}