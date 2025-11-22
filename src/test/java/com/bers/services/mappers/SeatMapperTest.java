package com.bers.services.mappers;

import com.bers.api.dtos.SeatDtos.*;
import com.bers.domain.entities.Bus;
import com.bers.domain.entities.Seat;
import com.bers.domain.entities.enums.BusStatus;
import com.bers.domain.entities.enums.SeatType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("SeatMapper Tests")
class SeatMapperTest {
    private SeatMapper seatMapper;

    @BeforeEach
    void setUp() {
        seatMapper = Mappers.getMapper(SeatMapper.class);
    }

    @Test
    @DisplayName("Debe mapear SeatCreateRequest a la entidad Seat")
    void shouldMapCreateRequestToEntity() {
        SeatCreateRequest request = new SeatCreateRequest(
                "1A",
                SeatType.STANDARD,
                1L
        );

        Seat seat = seatMapper.toEntity(request);

        assertNotNull(seat);
        Assertions.assertEquals("1A", seat.getNumber());
        Assertions.assertEquals(SeatType.STANDARD, seat.getType());
        assertNotNull(seat.getBus());
        Assertions.assertEquals(1L, seat.getBus().getId());
        assertNull(seat.getId());
    }

    @Test
    @DisplayName("Debe actualizar la entidad Seat desde SeatUpdateRequest")
    void shouldUpdateEntityFromUpdateRequest() {
        Seat existingSeat = Seat.builder()
                .id(1L)
                .number("1A")
                .type(SeatType.STANDARD)
                .build();

        SeatUpdateRequest request = new SeatUpdateRequest(
                SeatType.PREFERENTIAL
        );

        seatMapper.updateEntity(request, existingSeat);

        Assertions.assertEquals(SeatType.PREFERENTIAL, existingSeat.getType());
        Assertions.assertEquals("1A", existingSeat.getNumber());
    }

    @Test
    @DisplayName("Debe mapear la entidad Seat a SeatResponse")
    void shouldMapEntityToResponse() {
        Bus bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .amenities(new HashMap<>())
                .status(BusStatus.ACTIVE)
                .build();

        Seat seat = Seat.builder()
                .id(1L)
                .number("1A")
                .type(SeatType.PREFERENTIAL)
                .bus(bus)
                .build();

        SeatResponse response = seatMapper.toResponse(seat);

        assertNotNull(response);
        Assertions.assertEquals(1L, response.id());
        Assertions.assertEquals("1A", response.number());
        Assertions.assertEquals("PREFERENTIAL", response.type());
        Assertions.assertEquals(1L, response.busId());
        Assertions.assertEquals("ABC123", response.busPlate());
        Assertions.assertEquals(40, response.busCapacity());
    }

    @Test
    @DisplayName("Debe mapear todos los tipos de SeatType correctamente")
    void shouldMapAllSeatTypes() {
        for (SeatType type : SeatType.values()) {
            SeatCreateRequest request = new SeatCreateRequest(
                    "TEST",
                    type,
                    1L
            );

            Seat seat = seatMapper.toEntity(request);
            seat.setId(1L);
            seat.setBus(Bus.builder()
                    .id(1L)
                    .plate("TEST")
                    .capacity(40)
                    .status(BusStatus.ACTIVE)
                    .build());

            SeatResponse response = seatMapper.toResponse(seat);

            Assertions.assertEquals(type, seat.getType());
            Assertions.assertEquals(type.name(), response.type());
        }
    }

    @Test
    @DisplayName("Debe manejar diferentes formatos de número de asiento")
    void shouldHandleDifferentSeatNumberFormats() {
        String[] seatNumbers = {"1", "1A", "A1", "10", "10B", "VIP-1", "PREF_5"};

        for (String number : seatNumbers) {
            SeatCreateRequest request = new SeatCreateRequest(
                    number,
                    SeatType.STANDARD,
                    1L
            );

            Seat seat = seatMapper.toEntity(request);

            Assertions.assertEquals(number, seat.getNumber());
        }
    }

    @Test
    @DisplayName("Debe preservar la referencia al autobús durante el mapeo")
    void shouldPreserveBusReferenceDuringMapping() {
        Long busId = 42L;
        SeatCreateRequest request = new SeatCreateRequest(
                "1A",
                SeatType.STANDARD,
                busId
        );

        Seat seat = seatMapper.toEntity(request);

        assertNotNull(seat.getBus());
        Assertions.assertEquals(busId, seat.getBus().getId());
    }

    @Test
    @DisplayName("Debe mapear el asiento con información completa del autobús")
    void shouldMapSeatWithCompleteBusInformation() {
        Bus bus = Bus.builder()
                .id(10L)
                .plate("LUXURY001")
                .capacity(45)
                .amenities(new HashMap<>())
                .status(BusStatus.ACTIVE)
                .build();

        Seat seat = Seat.builder()
                .id(5L)
                .number("VIP-1")
                .type(SeatType.PREFERENTIAL)
                .bus(bus)
                .build();

        SeatResponse response = seatMapper.toResponse(seat);

        Assertions.assertEquals(5L, response.id());
        Assertions.assertEquals("VIP-1", response.number());
        Assertions.assertEquals("PREFERENTIAL", response.type());
        Assertions.assertEquals(10L, response.busId());
        Assertions.assertEquals("LUXURY001", response.busPlate());
        Assertions.assertEquals(45, response.busCapacity());
    }

    @Test
    @DisplayName("Debe manejar el tipo de asiento 'standard' como predeterminado")
    void shouldHandleStandardSeatTypeAsDefault() {
        SeatCreateRequest request = new SeatCreateRequest(
                "1A",
                SeatType.STANDARD,
                1L
        );

        Seat seat = seatMapper.toEntity(request);

        Assertions.assertEquals(SeatType.STANDARD, seat.getType());
    }

    @Test
    @DisplayName("Debe actualizar solo el campo de tipo")
    void shouldUpdateOnlyTypeField() {
        Bus originalBus = Bus.builder()
                .id(1L)
                .plate("ORIGINAL")
                .build();

        Seat existingSeat = Seat.builder()
                .id(1L)
                .number("ORIGINAL_NUMBER")
                .type(SeatType.STANDARD)
                .bus(originalBus)
                .build();

        SeatUpdateRequest request = new SeatUpdateRequest(
                SeatType.PREFERENTIAL
        );

        seatMapper.updateEntity(request, existingSeat);

        Assertions.assertEquals(SeatType.PREFERENTIAL, existingSeat.getType());
        Assertions.assertEquals("ORIGINAL_NUMBER", existingSeat.getNumber());
        Assertions.assertEquals(originalBus, existingSeat.getBus());
        Assertions.assertEquals(1L, existingSeat.getId());
    }

    @Test
    @DisplayName("Debe preservar todos los campos durante el ciclo completo de mapeo")
    void shouldPreserveAllFieldsDuringFullCycleMapping() {
        SeatCreateRequest request = new SeatCreateRequest(
                "FULL-TEST-1",
                SeatType.PREFERENTIAL,
                999L
        );

        Seat seat = seatMapper.toEntity(request);
        seat.setId(100L);
        seat.setBus(Bus.builder()
                .id(999L)
                .plate("TEST-PLATE")
                .capacity(50)
                .status(BusStatus.ACTIVE)
                .build());

        SeatResponse response = seatMapper.toResponse(seat);

        Assertions.assertEquals(100L, response.id());
        Assertions.assertEquals("FULL-TEST-1", response.number());
        Assertions.assertEquals("PREFERENTIAL", response.type());
        Assertions.assertEquals(999L, response.busId());
        Assertions.assertEquals("TEST-PLATE", response.busPlate());
        Assertions.assertEquals(50, response.busCapacity());
    }
}