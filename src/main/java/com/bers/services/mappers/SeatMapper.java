package com.bers.services.mappers;

import com.bers.api.dtos.SeatDtos.SeatCreateRequest;
import com.bers.api.dtos.SeatDtos.SeatResponse;
import com.bers.api.dtos.SeatDtos.SeatStatusResponse;
import com.bers.api.dtos.SeatDtos.SeatUpdateRequest;
import com.bers.domain.entities.Bus;
import com.bers.domain.entities.Seat;
import com.bers.domain.repositories.SeatHoldRepository;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SeatMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "number", source = "number")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "bus", source = "busId", qualifiedByName = "mapBus")
    Seat toEntity(SeatCreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "number", ignore = true)
    @Mapping(target = "bus", ignore = true)
    @Mapping(target = "type", source = "type")
    void updateEntity(SeatUpdateRequest dto, @MappingTarget Seat seat);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "number", source = "number")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "busId", source = "bus.id")
    @Mapping(target = "busPlate", source = "bus.plate")
    @Mapping(target = "busCapacity", source = "bus.capacity")
    SeatResponse toResponse(Seat entity);

    @Mapping(target = "seatId", source = "entity.id")
    @Mapping(target = "seatNumber", source = "entity.number")
    @Mapping(
            target = "isHeld",
            expression = "java(seatHoldRepository.existsByTripIdAndSeatNumberAndStatus(tripId, entity.getNumber(), com.bers.domain.entities.enums.HoldStatus.HOLD))"
    )
    @Mapping(target = "isOccupied", expression = "java(false)")
    @Mapping(target = "available", expression = "java(false)")
    SeatStatusResponse toStatusResponse(
            Seat entity,
            Long tripId,
            @Context SeatHoldRepository seatHoldRepository
    );

    @Named("mapBus")
    default Bus mapBus(Long id) {
        if (id == null) return null;
        Bus b = new Bus();
        b.setId(id);
        return b;
    }
}