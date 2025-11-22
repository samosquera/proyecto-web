package com.bers.services.mappers;

import com.bers.api.dtos.BusDtos.BusCreateRequest;
import com.bers.api.dtos.BusDtos.BusResponse;
import com.bers.api.dtos.BusDtos.BusUpdateRequest;
import com.bers.api.dtos.BusDtos.BusWithSeatsResponse;
import com.bers.domain.entities.Bus;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BusMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "plate", source = "plate")
    @Mapping(target = "capacity", source = "capacity")
    @Mapping(target = "amenities", source = "amenities")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "seats", ignore = true)
    Bus toEntity(BusCreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "plate", ignore = true)
    @Mapping(target = "seats", ignore = true)
    @Mapping(target = "capacity", source = "capacity")
    @Mapping(target = "amenities", source = "amenities")
    @Mapping(target = "status", source = "status")
    void updateEntity(BusUpdateRequest dto, @MappingTarget Bus bus);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "plate", source = "plate")
    @Mapping(target = "capacity", source = "capacity")
    @Mapping(target = "amenities", source = "amenities")
    @Mapping(target = "status", source = "status")
    BusResponse toResponse(Bus entity);

    default BusWithSeatsResponse toResponseWithSeats(Bus bus, Integer availableSeats) {
        if (bus == null) return null;

        return new BusWithSeatsResponse(
                bus.getId(),
                bus.getPlate(),
                bus.getCapacity(),
                bus.getAmenities(),
                bus.getStatus().name(),
                bus.getSeats() != null ? bus.getSeats().size() : 0,
                availableSeats != null ? availableSeats : 0
        );
    }
}