package com.bers.services.mappers;

import com.bers.api.dtos.TicketDtos.*;
import com.bers.domain.entities.Stop;
import com.bers.domain.entities.Ticket;
import com.bers.domain.entities.Trip;
import com.bers.domain.entities.User;
import org.mapstruct.*;

import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "status", expression = "java(com.bers.domain.entities.enums.TicketStatus.SOLD)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "passengerType", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "refundAmount", ignore = true)
    @Mapping(target = "cancellationPolicy", ignore = true)
    @Mapping(target = "seatNumber", source = "seatNumber")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    @Mapping(target = "qrCode", expression = "java(generateQRCode(dto))")
    @Mapping(target = "trip", source = "tripId", qualifiedByName = "mapTrip")
    @Mapping(target = "passenger", source = "passengerId", qualifiedByName = "mapUser")
    @Mapping(target = "fromStop", source = "fromStopId", qualifiedByName = "mapStop")
    @Mapping(target = "toStop", source = "toStopId", qualifiedByName = "mapStop")
    Ticket toEntity(TicketCreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "seatNumber", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "qrCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "passengerType", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "refundAmount", ignore = true)
    @Mapping(target = "cancellationPolicy", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "passenger", ignore = true)
    @Mapping(target = "fromStop", ignore = true)
    @Mapping(target = "toStop", ignore = true)
    @Mapping(target = "status", source = "status")
    void updateEntity(TicketUpdateRequest dto, @MappingTarget Ticket entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "seatNumber", source = "seatNumber")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "qrCode", source = "qrCode")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "cancelledAt", source = "cancelledAt")
    @Mapping(target = "refundAmount", source = "refundAmount")
    @Mapping(target = "cancellationPolicy", source = "cancellationPolicy")
    @Mapping(target = "tripId", source = "trip.id")
    @Mapping(target = "tripDate", source = "trip.date", qualifiedByName = "formatDate")
    @Mapping(target = "tripTime", source = "trip.departureAt", qualifiedByName = "formatTime")
    @Mapping(target = "routeOrigin", source = "entity", qualifiedByName = "getRouteOrigin")
    @Mapping(target = "routeDestination", source = "entity", qualifiedByName = "getRouteDestination")
    @Mapping(target = "busPlate", source = "entity", qualifiedByName = "getBusPlate")
    @Mapping(target = "passengerId", source = "passenger.id")
    @Mapping(target = "passengerName", source = "passenger.username")
    @Mapping(target = "fromStopId", source = "fromStop.id")
    @Mapping(target = "fromStopName", source = "fromStop.name")
    @Mapping(target = "toStopId", source = "toStop.id")
    @Mapping(target = "toStopName", source = "toStop.name")
    TicketResponse toResponse(Ticket entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "seatNumber", source = "seatNumber")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "qrCode", source = "qrCode")
    @Mapping(target = "tripDate", source = "trip.departureAt", dateFormat = "yyyy-MM-dd")
    @Mapping(target = "tripTime", source = "trip.departureAt", dateFormat = "HH:mm")
    @Mapping(target = "fromStopName", source = "fromStop.name")
    @Mapping(target = "toStopName", source = "toStop.name")
    @Mapping(target = "passengerName", source = "passenger.username")
    TicketSummaryResponse toSummaryResponse(Ticket entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "seatNumber", source = "seatNumber")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "passengerType", source = "passengerType")
    @Mapping(target = "discountAmount", source = "discountAmount")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "cancelledAt", source = "cancelledAt")
    @Mapping(target = "refundAmount", source = "refundAmount")
    @Mapping(target = "cancellationPolicy", source = "cancellationPolicy")
    @Mapping(target = "tripId", source = "trip.id")
    @Mapping(target = "passengerName", source = "passenger.username")
    TicketAdminResponse toAdminResponse(Ticket entity);

    @Named("mapTrip")
    default Trip mapTrip(Long id) {
        if (id == null) return null;
        Trip t = new Trip();
        t.setId(id);
        return t;
    }

    @Named("mapUser")
    default User mapUser(Long id) {
        if (id == null) return null;
        User u = new User();
        u.setId(id);
        return u;
    }

    @Named("mapStop")
    default Stop mapStop(Long id) {
        if (id == null) return null;
        Stop s = new Stop();
        s.setId(id);
        return s;
    }

    @Named("formatDate")
    default String formatDate(java.time.LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }

    @Named("formatTime")
    default String formatTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern("HH:mm")) : null;
    }

    default String generateQRCode(TicketCreateRequest dto) {
        return "TICKET-" + dto.tripId() + "-" + dto.seatNumber() + "-" +
                java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Named("getRouteOrigin")

    default String getRouteOrigin(Ticket ticket) {

        if (ticket == null || ticket.getTrip() == null || ticket.getTrip().getRoute() == null) {

            return null;

        }

        return ticket.getTrip().getRoute().getOrigin();

    }

    @Named("getRouteDestination")

    default String getRouteDestination(Ticket ticket) {

        if (ticket == null || ticket.getTrip() == null || ticket.getTrip().getRoute() == null) {

            return null;

        }

        return ticket.getTrip().getRoute().getDestination();

    }


    @Named("getBusPlate")

    default String getBusPlate(Ticket ticket) {

        if (ticket == null || ticket.getTrip() == null || ticket.getTrip().getBus() == null) {

            return null;

        }

        return ticket.getTrip().getBus().getPlate();

    }
}