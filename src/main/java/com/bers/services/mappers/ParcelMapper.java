package com.bers.services.mappers;

import com.bers.api.dtos.ParcelDtos.ParcelCreateRequest;
import com.bers.api.dtos.ParcelDtos.ParcelResponse;
import com.bers.api.dtos.ParcelDtos.ParcelUpdateRequest;
import com.bers.domain.entities.Parcel;
import com.bers.domain.entities.Stop;
import com.bers.domain.entities.Trip;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ParcelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    @Mapping(target = "code", expression = "java(generateParcelCode())")
    @Mapping(target = "senderName", source = "senderName")
    @Mapping(target = "senderPhone", source = "senderPhone")
    @Mapping(target = "receiverName", source = "receiverName")
    @Mapping(target = "receiverPhone", source = "receiverPhone")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "status", expression = "java(com.bers.domain.entities.enums.ParcelStatus.CREATED)")
    @Mapping(target = "proofPhotoUrl", ignore = true)
    @Mapping(target = "deliveryOtp", expression = "java(generateOTP())")
    @Mapping(target = "fromStop", source = "fromStopId", qualifiedByName = "mapStop")
    @Mapping(target = "toStop", source = "toStopId", qualifiedByName = "mapStop")
    @Mapping(target = "trip", source = "tripId", qualifiedByName = "mapTrip")
    Parcel toEntity(ParcelCreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "senderName", ignore = true)
    @Mapping(target = "senderPhone", ignore = true)
    @Mapping(target = "receiverName", ignore = true)
    @Mapping(target = "receiverPhone", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    @Mapping(target = "fromStop", ignore = true)
    @Mapping(target = "toStop", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "proofPhotoUrl", source = "proofPhotoUrl")
    @Mapping(target = "deliveryOtp", source = "deliveryOtp")
    void updateEntity(ParcelUpdateRequest dto, @MappingTarget Parcel parcel);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "senderName", source = "senderName")
    @Mapping(target = "senderPhone", source = "senderPhone")
    @Mapping(target = "receiverName", source = "receiverName")
    @Mapping(target = "receiverPhone", source = "receiverPhone")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "proofPhotoUrl", source = "proofPhotoUrl")
    @Mapping(target = "deliveryOtp", source = "deliveryOtp")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "deliveredAt", source = "deliveredAt")
    @Mapping(target = "fromStopId", source = "fromStop.id")
    @Mapping(target = "toStopId", source = "toStop.id")
    @Mapping(target = "tripId", source = "trip.id")
    ParcelResponse toResponse(Parcel entity);

    @Named("mapStop")
    default Stop mapStop(Long id) {
        if (id == null) return null;
        Stop s = new Stop();
        s.setId(id);
        return s;
    }

    @Named("mapTrip")
    default Trip mapTrip(Long id) {
        if (id == null) return null;
        Trip t = new Trip();
        t.setId(id);
        return t;
    }

    default String generateParcelCode() {
        return "PCL-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
    }

    default String generateOTP() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}