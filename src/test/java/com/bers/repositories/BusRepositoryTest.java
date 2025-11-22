package com.bers.repositories;

import com.bers.AbstractRepositoryTest;
import com.bers.domain.entities.Bus;
import com.bers.domain.entities.Seat;
import com.bers.domain.entities.enums.BusStatus;
import com.bers.domain.entities.enums.SeatType;
import com.bers.domain.repositories.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BusRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private BusRepository busRepository;

    @Test
    @DisplayName("Guardar un bus")
    void shouldSaveBus() {
        var bus = createBus("ABC123",40,BusStatus.ACTIVE);

        Map<String, Object> amenities = new HashMap<>();
        amenities.put("wifi", true);
        amenities.put("ac", true);
        bus.setAmenities(amenities);

        Bus saved = busRepository.save(bus);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPlate()).isEqualTo("ABC123");
        assertThat(saved.getAmenities()).containsKey("wifi");
    }

    @Test
    @DisplayName("Buscar bus por plate")
    void shouldFindBusByPlate() {
        var bus =createBus("XYZ789",45,BusStatus.ACTIVE);
        busRepository.save(bus);

        Optional<Bus> found = busRepository.findByPlate("XYZ789");

        assertThat(found).isPresent();
        assertThat(found.get().getCapacity()).isEqualTo(45);
    }

    @Test
    @DisplayName("Listar buses por status")
    void shouldFindBusesByStatus() {
        Bus activeBus1 = createBus("ACTIVE1",40, BusStatus.ACTIVE);
        Bus activeBus2 = createBus("ACTIVE2", 35, BusStatus.ACTIVE);
        Bus maintenanceBus = createBus("MAINT", 50 , BusStatus.MAINTENANCE);

        busRepository.saveAll(List.of(activeBus1, activeBus2, maintenanceBus));

        List<Bus> activeBuses = busRepository.findByStatus(BusStatus.ACTIVE);

        assertThat(activeBuses).hasSize(2);
        assertThat(activeBuses).extracting(Bus::getStatus)
                .containsOnly(BusStatus.ACTIVE);
    }

    @Test
    @DisplayName("Buscar bus por id, incluyendo sus seats")
    void shouldFindBusByIdWithSeats() {
        var bus = createBus("BUS-SEATS",40, BusStatus.ACTIVE);

        var seat1 = Seat.builder()
                .bus(bus)
                .number("A1")
                .type(SeatType.STANDARD)
                .build();
        var seat2 = Seat.builder()
                .bus(bus)
                .number("A2")
                .type(SeatType.STANDARD)
                .build();
        var seat3 = Seat.builder()
                .bus(bus)
                .number("A3")
                .type(SeatType.PREFERENTIAL)
                .build();
        var seat4 = Seat.builder()
                .bus(bus)
                .number("A4")
                .type(SeatType.PREFERENTIAL)
                .build();



        bus.getSeats().addAll(List.of(seat1, seat2, seat3, seat4));
        Bus saved = busRepository.save(bus);

        Optional<Bus> found = busRepository.findByIdWithSeats(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSeats()).hasSize(4);
        assertThat(found.get().getSeats()).extracting(Seat::getNumber)
                .containsExactlyInAnyOrder("A1", "A2", "A3", "A4");
    }

    @Test
    @DisplayName("Buscar buses activos con capacidad minima")
    void shouldFindAvailableBusesByCapacity() {
        Bus smallBus = createBus("ANB233",20 ,BusStatus.ACTIVE);

        Bus mediumBus = createBus("ALL234", 40, BusStatus.ACTIVE);

        Bus largeBus = createBus("LAR344", 50 ,BusStatus.ACTIVE);

        Bus inactiveBus = createBus("PZR456", 60, BusStatus.INACTIVE);


        busRepository.saveAll(List.of(smallBus, mediumBus, largeBus, inactiveBus));

        List<Bus> buses = busRepository.findAvailableBusesByCapacity(
                BusStatus.ACTIVE, 35);

        assertThat(buses).hasSize(2);
        assertThat(buses).extracting(Bus::getPlate)
                .containsExactlyInAnyOrder("ALL234", "LAR344");
    }

    @Test
    @DisplayName("Verificar si una plate ya existe")
    void shouldCheckIfPlateExists() {
        Bus bus = createBus("CHE123", 40, BusStatus.ACTIVE);
        busRepository.save(bus);

        boolean exists = busRepository.existsByPlate("CHE123");
        boolean notExists = busRepository.existsByPlate("NAW123");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    private Bus createBus(String plate,Integer capacity, BusStatus status) {
        return Bus.builder()
                .plate(plate)
                .capacity(capacity)
                .status(status)
                .build();
    }
}