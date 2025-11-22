package com.bers.services.mappers;

import com.bers.api.dtos.BusDtos.*;
import com.bers.domain.entities.Bus;
import com.bers.domain.entities.Seat;
import com.bers.domain.entities.enums.BusStatus;
import com.bers.domain.entities.enums.SeatType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BusMapper Tests")
class BusMapperTest {

    private BusMapper busMapper;

    @BeforeEach
    void setUp() {
        busMapper = Mappers.getMapper(BusMapper.class);
    }

    @Test
    @DisplayName("Debe mapear BusCreateRequest a la entidad Bus")
    void shouldMapCreateRequestToEntity() {
        Map<String, Object> amenities = new HashMap<>();
        amenities.put("wifi", true);
        amenities.put("ac", true);
        amenities.put("tv", false);

        BusCreateRequest request = new BusCreateRequest(
                "ABC123",
                45,
                amenities,
                BusStatus.ACTIVE
        );

        Bus bus = busMapper.toEntity(request);

        assertNotNull(bus);
        Assertions.assertEquals("ABC123", bus.getPlate());
        Assertions.assertEquals(45, bus.getCapacity());
        Assertions.assertEquals(BusStatus.ACTIVE, bus.getStatus());
        assertNotNull(bus.getAmenities());
        Assertions.assertEquals(3, bus.getAmenities().size());
        Assertions.assertTrue((Boolean) bus.getAmenities().get("wifi"));
        assertNull(bus.getId());
    }

    @Test
    @DisplayName("Debe actualizar la entidad Bus desde BusUpdateRequest")
    void shouldUpdateEntityFromUpdateRequest() {
        Map<String, Object> oldAmenities = new HashMap<>();
        oldAmenities.put("wifi", false);

        Bus existingBus = Bus.builder()
                .id(1L)
                .plate("OLD123")
                .capacity(40)
                .amenities(oldAmenities)
                .status(BusStatus.ACTIVE)
                .build();

        Map<String, Object> newAmenities = new HashMap<>();
        newAmenities.put("wifi", true);
        newAmenities.put("bathroom", true);

        BusUpdateRequest request = new BusUpdateRequest(
                50,
                newAmenities,
                BusStatus.MAINTENANCE
        );

        busMapper.updateEntity(request, existingBus);

        Assertions.assertEquals(50, existingBus.getCapacity());
        Assertions.assertEquals(BusStatus.MAINTENANCE, existingBus.getStatus());
        Assertions.assertEquals(2, existingBus.getAmenities().size());
        Assertions.assertTrue((Boolean) existingBus.getAmenities().get("wifi"));
        Assertions.assertEquals("OLD123", existingBus.getPlate());
    }

    @Test
    @DisplayName("Debe mapear la entidad Bus a BusResponse")
    void shouldMapEntityToResponse() {
        Map<String, Object> amenities = new HashMap<>();
        amenities.put("wifi", true);
        amenities.put("ac", true);

        Bus bus = Bus.builder()
                .id(1L)
                .plate("XYZ789")
                .capacity(40)
                .amenities(amenities)
                .status(BusStatus.ACTIVE)
                .build();

        BusResponse response = busMapper.toResponse(bus);

        assertNotNull(response);
        Assertions.assertEquals(1L, response.id());
        Assertions.assertEquals("XYZ789", response.plate());
        Assertions.assertEquals(40, response.capacity());
        Assertions.assertEquals("ACTIVE", response.status());
    }

    @Test
    @DisplayName("Debe mapear Bus con asientos a BusWithSeatsResponse")
    void shouldMapBusWithSeatsToResponse() {
        Bus bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(45)
                .amenities(new HashMap<>())
                .status(BusStatus.ACTIVE)
                .build();

        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= 45; i++) {
            seats.add(Seat.builder()
                    .id((long) i)
                    .number(String.valueOf(i))
                    .type(SeatType.STANDARD)
                    .bus(bus)
                    .build());
        }
        bus.setSeats(seats);

        Integer availableSeats = 30;

        BusWithSeatsResponse response = busMapper.toResponseWithSeats(bus, availableSeats);

        assertNotNull(response);
        Assertions.assertEquals(1L, response.id());
        Assertions.assertEquals("ABC123", response.plate());
        Assertions.assertEquals(45, response.capacity());
        Assertions.assertEquals("ACTIVE", response.status());
        Assertions.assertEquals(45, response.totalSeats());
        Assertions.assertEquals(30, response.availableSeats());
    }

    @Test
    @DisplayName("Debe manejar un mapa de comodidades vacío")
    void shouldHandleEmptyAmenitiesMap() {
        BusCreateRequest request = new BusCreateRequest(
                "TEST123",
                40,
                new HashMap<>(),
                BusStatus.ACTIVE
        );

        Bus bus = busMapper.toEntity(request);

        assertNotNull(bus);
        assertNotNull(bus.getAmenities());
        Assertions.assertTrue(bus.getAmenities().isEmpty());
    }

    @Test
    @DisplayName("Debe manejar un mapa de comodidades nulo")
    void shouldHandleNullAmenitiesMap() {
        BusCreateRequest request = new BusCreateRequest(
                "TEST123",
                40,
                null,
                BusStatus.ACTIVE
        );

        Bus bus = busMapper.toEntity(request);

        assertNotNull(bus);
    }

    @Test
    @DisplayName("Debe mapear todos los tipos de BusStatus correctamente")
    void shouldMapAllBusStatusTypes() {
        for (BusStatus status : BusStatus.values()) {
            BusCreateRequest request = new BusCreateRequest(
                    "TEST" + status,
                    40,
                    new HashMap<>(),
                    status
            );

            Bus bus = busMapper.toEntity(request);
            bus.setId(1L);
            BusResponse response = busMapper.toResponse(bus);

            Assertions.assertEquals(status, bus.getStatus());
            Assertions.assertEquals(status.name(), response.status());
        }
    }

    @Test
    @DisplayName("Debe manejar estructuras de comodidades complejas")
    void shouldHandleComplexAmenitiesStructures() {
        Map<String, Object> amenities = new HashMap<>();
        amenities.put("wifi", true);
        amenities.put("ac", true);
        amenities.put("seats", Map.of("reclining", true, "leather", false));
        amenities.put("entertainment", List.of("tv", "radio", "movies"));
        amenities.put("capacity_details", Map.of("standard", 40, "preferential", 5));

        BusCreateRequest request = new BusCreateRequest(
                "LUXURY001",
                45,
                amenities,
                BusStatus.ACTIVE
        );

        Bus bus = busMapper.toEntity(request);

        assertNotNull(bus);
        assertNotNull(bus.getAmenities());
        Assertions.assertEquals(5, bus.getAmenities().size());
        Assertions.assertTrue((Boolean) bus.getAmenities().get("wifi"));
        assertNotNull(bus.getAmenities().get("seats"));
        assertNotNull(bus.getAmenities().get("entertainment"));
    }

    @Test
    @DisplayName("Debe manejar autobús sin asientos en BusWithSeatsResponse")
    void shouldHandleBusWithoutSeatsInWithSeatsResponse() {
        Bus bus = Bus.builder()
                .id(1L)
                .plate("EMPTY001")
                .capacity(45)
                .amenities(new HashMap<>())
                .status(BusStatus.ACTIVE)
                .seats(new ArrayList<>())
                .build();

        Integer availableSeats = 45;

        BusWithSeatsResponse response = busMapper.toResponseWithSeats(bus, availableSeats);

        assertNotNull(response);
        Assertions.assertEquals(0, response.totalSeats());
        Assertions.assertEquals(45, response.availableSeats());
    }

    @Test
    @DisplayName("Debe manejar autobús nulo en BusWithSeatsResponse")
    void shouldHandleNullBusInWithSeatsResponse() {
        BusWithSeatsResponse response = busMapper.toResponseWithSeats(null, 0);

        assertNull(response);
    }

    @Test
    @DisplayName("Debe preservar todos los campos durante el ciclo completo de mapeo")
    void shouldPreserveAllFieldsDuringFullCycleMapping() {
        Map<String, Object> amenities = new HashMap<>();
        amenities.put("wifi", true);
        amenities.put("ac", false);

        BusCreateRequest request = new BusCreateRequest(
                "FULL123",
                42,
                amenities,
                BusStatus.MAINTENANCE
        );

        Bus bus = busMapper.toEntity(request);
        bus.setId(999L);
        BusResponse response = busMapper.toResponse(bus);

        Assertions.assertEquals(999L, response.id());
        Assertions.assertEquals("FULL123", response.plate());
        Assertions.assertEquals(42, response.capacity());
        Assertions.assertEquals("MAINTENANCE", response.status());
    }
}