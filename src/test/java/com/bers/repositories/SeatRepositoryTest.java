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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SeatRepositoryTest  extends AbstractRepositoryTest {
    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BusRepository busRepository;

    @Test
    @DisplayName("Guardar un seat")
    void shouldSaveSeat() {
        var bus = createBus("BUS-SEAT");
        var savedBus = busRepository.save(bus);

        var seat = createSeat(savedBus,"A1",SeatType.STANDARD);

        var saved = seatRepository.save(seat);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNumber()).isEqualTo("A1");
    }

    @Test
    @DisplayName("Buscar todos los seat de un id de bus")
    void shouldFindSeatsByBusId() {

        var bus = createBus("BUS-MULTI");
        var savedBus = busRepository.save(bus);

        seatRepository.save(createSeat(savedBus, "A1", SeatType.STANDARD));
        seatRepository.save(createSeat(savedBus, "A2", SeatType.STANDARD));
        seatRepository.save(createSeat(savedBus, "B1", SeatType.PREFERENTIAL));

        List<Seat> seats = seatRepository.findByBusId(savedBus.getId());

        assertThat(seats).hasSize(3);
    }

    @Test
    @DisplayName("Buscar los seat de un bus y ordenarlos por number")
    void shouldFindSeatsByBusIdOrderedByNumber() {
        Bus bus = createBus("BUS-ORDER");
        Bus savedBus = busRepository.save(bus);

        seatRepository.save(createSeat(savedBus, "C1", SeatType.STANDARD));
        seatRepository.save(createSeat(savedBus, "A1", SeatType.STANDARD));
        seatRepository.save(createSeat(savedBus, "B1", SeatType.STANDARD));

        List<Seat> seats = seatRepository.findByBusIdOrderByNumberAsc(savedBus.getId());

        assertThat(seats).hasSize(3);
        assertThat(seats.get(0).getNumber()).isEqualTo("A1");
        assertThat(seats.get(1).getNumber()).isEqualTo("B1");
        assertThat(seats.get(2).getNumber()).isEqualTo("C1");
    }

    @Test
    @DisplayName("Buscar un seat por id de bus y number")
    void shouldFindSeatByBusIdAndNumber() {
        // Given
        Bus bus = createBus("BUS-FIND");
        Bus savedBus = busRepository.save(bus);
        seatRepository.save(createSeat(savedBus, "D5", SeatType.STANDARD));

        Optional<Seat> found = seatRepository.findByBusIdAndNumber(savedBus.getId(), "D5");

        assertThat(found).isPresent();
        assertThat(found.get().getNumber()).isEqualTo("D5");
    }

    @Test
    @DisplayName("Buscar seat por id de bus y type")
    void shouldFindSeatsByBusIdAndType() {
        Bus bus = createBus("BUS-TYPE");
        Bus savedBus = busRepository.save(bus);

        seatRepository.save(createSeat(savedBus, "A1", SeatType.STANDARD));
        seatRepository.save(createSeat(savedBus, "A2", SeatType.STANDARD));
        seatRepository.save(createSeat(savedBus, "B1", SeatType.PREFERENTIAL));

        List<Seat> standardSeats = seatRepository.findByBusIdAndType(
                savedBus.getId(), SeatType.STANDARD);
        List<Seat> preferentialSeats = seatRepository.findByBusIdAndType(
                savedBus.getId(), SeatType.PREFERENTIAL);

        assertThat(standardSeats).hasSize(2);
        assertThat(preferentialSeats).hasSize(1);
    }

    @Test
    @DisplayName("Contar el total de asientos de un bus")
    void shouldCountSeatsByBusId() {
        Bus bus = createBus("BUS-COUNT");
        Bus savedBus = busRepository.save(bus);

        seatRepository.save(createSeat(savedBus, "A1", SeatType.STANDARD));
        seatRepository.save(createSeat(savedBus, "A2", SeatType.STANDARD));
        seatRepository.save(createSeat(savedBus, "A3", SeatType.STANDARD));

        long count = seatRepository.countByBusId(savedBus.getId());
        assertThat(count).isEqualTo(3);
    }

    private Bus createBus(String plate) {
        return Bus.builder()
                .plate(plate)
                .capacity(40)
                .status(BusStatus.ACTIVE)
                .build();
    }

    private Seat createSeat(Bus bus, String number, SeatType type) {
        return Seat.builder()
                .bus(bus)
                .number(number)
                .type(type)
                .build();
    }
}