package com.bers.repositories;

import com.bers.AbstractRepositoryTest;
import com.bers.domain.entities.*;
import com.bers.domain.repositories.*;
import com.bers.domain.entities.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BaggageRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private BaggageRepository baggageRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private StopRepository stopRepository;

    @Test
    @DisplayName("Guardar un baggage")
    void shouldSaveBaggage() {
        var ticket = createAndSaveTicket();
        var baggage = createBaggage(ticket,"23.5","5000","BAG-147");

        Baggage saved = baggageRepository.save(baggage);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getWeightKg()).isEqualByComparingTo(new BigDecimal("23.5"));
        assertThat(saved.getFee()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(saved.getTagCode()).isNotNull();
    }

    @Test
    @DisplayName("Buscar baggage por code de tag")
    void shouldFindBaggageByTagCode() {
        var ticket = createAndSaveTicket();
        var tagCode = "TAG-123";

        var baggage = createBaggage(ticket,"20.0","0",tagCode);
        baggageRepository.save(baggage);

        Optional<Baggage> found = baggageRepository.findByTagCode(tagCode);

        assertThat(found).isPresent();
        assertThat(found.get().getWeightKg()).isEqualByComparingTo(new BigDecimal("20.0"));
    }

    @Test
    @DisplayName("Buscar baggage pro code que no existe")
    void shouldNotFindBaggageByNonExistentTagCode() {
        Optional<Baggage> found = baggageRepository.findByTagCode("NON-EXISTENT-TAG");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Listar baggage por id de ticket")
    void shouldFindBaggagesByTicketId() {
        Ticket ticket = createAndSaveTicket();

        Baggage baggage1 = createBaggage(ticket, "15.0", "0", "TAG-1");
        Baggage baggage2 = createBaggage(ticket, "25.0", "5000", "TAG-2");
        baggageRepository.saveAll(List.of(baggage1, baggage2));

        List<Baggage> baggages = baggageRepository.findByTicketId(ticket.getId());

        assertThat(baggages).hasSize(2);
        assertThat(baggages).extracting(Baggage::getTagCode)
                .containsExactlyInAnyOrder("TAG-1", "TAG-2");
    }

    @Test
    @DisplayName("Listar baggage por id de viaje")
    void shouldFindBaggagesByTripId() {
        Trip trip = createAndSaveTrip();

        Ticket ticket1 = createTicketForTrip(trip, "passenger1@example.com", "3001111111");
        Ticket ticket2 = createTicketForTrip(trip, "passenger2@example.com", "3002222222");

        baggageRepository.saveAll(List.of(
                createBaggage(ticket1, "20.0", "0", "TRIP-TAG-1"),
                createBaggage(ticket1, "10.0", "0", "TRIP-TAG-2"),
                createBaggage(ticket2, "30.0", "10000", "TRIP-TAG-3")
        ));

        List<Baggage> baggages = baggageRepository.findByTripId(trip.getId());

        assertThat(baggages).hasSize(3);
    }

    @Test
    @DisplayName("Calcular peso total equipaje")
    void shouldGetTotalWeightByTrip() {
        Trip trip = createAndSaveTrip();

        Ticket ticket1 = createTicketForTrip(trip, "weight1@example.com", "3003333333");
        Ticket ticket2 = createTicketForTrip(trip, "weight2@example.com", "3004444444");

        baggageRepository.saveAll(List.of(
                createBaggage(ticket1, "15.5", "0", "W-TAG-1"),
                createBaggage(ticket1, "20.0", "0", "W-TAG-2"),
                createBaggage(ticket2, "18.5", "0", "W-TAG-3")
        ));

        BigDecimal totalWeight = baggageRepository.getTotalWeightByTrip(trip.getId());

        assertThat(totalWeight).isEqualByComparingTo(new BigDecimal("54.0")); // 15.5 + 20.0 + 18.5
    }

    @Test
    @DisplayName("Retorna null si no hay equipaje para viaje")
    void shouldReturnNullWhenNoBaggageForTrip() {
        Trip trip = createAndSaveTrip();

        BigDecimal totalWeight = baggageRepository.getTotalWeightByTrip(trip.getId());

        assertThat(totalWeight).isNull();
    }

    @Test
    @DisplayName("Verificar si un codigo de etiqueta ya existe")
    void shouldCheckIfTagCodeExists() {
        Ticket ticket = createAndSaveTicket();
        String existingTag = "EXISTS-TAG";

        Baggage baggage = createBaggage(ticket, "20.0", "0", existingTag);
        baggageRepository.save(baggage);

        boolean exists = baggageRepository.existsByTagCode(existingTag);
        boolean notExists = baggageRepository.existsByTagCode("NOT-EXISTS-TAG");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Manejar multiples baggage para el mismo ticket")
    void shouldHandleMultipleBaggagesForSameTicket() {
        Ticket ticket = createAndSaveTicket();

        baggageRepository.saveAll(List.of(
                createBaggage(ticket, "10.0", "0", "MULTI-1"),
                createBaggage(ticket, "15.0", "0", "MULTI-2"),
                createBaggage(ticket, "25.0", "5000", "MULTI-3")
        ));

        List<Baggage> baggages = baggageRepository.findByTicketId(ticket.getId());

        assertThat(baggages).hasSize(3);
        BigDecimal totalWeight = baggages.stream()
                .map(Baggage::getWeightKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalWeight).isEqualByComparingTo(new BigDecimal("50.0"));
    }

    @Test
    @DisplayName("Manejar baggage con tarifa 0")
    void shouldHandleBaggageWithZeroFee() {
        Ticket ticket = createAndSaveTicket();

        var freeBaggage = createBaggage(ticket,"20.0","0","FREE-TAG");

        Baggage saved = baggageRepository.save(freeBaggage);

        assertThat(saved.getFee()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Manejar baggage por exceso peso")
    void shouldHandleBaggageWithExcessFee() {
        Ticket ticket = createAndSaveTicket();

        var excessBaggage =  createBaggage(ticket,"35.0","15000","EXCESS-TAG");

        Baggage saved = baggageRepository.save(excessBaggage);

        assertThat(saved.getWeightKg()).isEqualByComparingTo(new BigDecimal("35.0"));
        assertThat(saved.getFee()).isEqualByComparingTo(new BigDecimal("15000"));
    }

    @Test
    @DisplayName("Eliminar equipaje por id")
    void shouldDeleteBaggage() {
        Ticket ticket = createAndSaveTicket();
        Baggage baggage = createBaggage(ticket, "20.0", "0", "DELETE-TAG");
        Baggage saved = baggageRepository.save(baggage);

        baggageRepository.deleteById(saved.getId());

        Optional<Baggage> deleted = baggageRepository.findById(saved.getId());
        assertThat(deleted).isEmpty();
    }

    private Trip createAndSaveTrip() {
        Route route = Route.builder()
                .code("ROUTE-" + System.currentTimeMillis())
                .name("Test Route")
                .origin("Origin")
                .destination("Destination")
                .distanceKm(100)
                .durationMin(120)
                .build();
        Route savedRoute = routeRepository.save(route);

        Bus bus = Bus.builder()
                .plate("BUS-" + System.currentTimeMillis())
                .capacity(40)
                .status(BusStatus.ACTIVE)
                .build();
        Bus savedBus = busRepository.save(bus);

        Trip trip = Trip.builder()
                .route(savedRoute)
                .bus(savedBus)
                .date(LocalDate.now())
                .departureAt(LocalDateTime.now().plusHours(2))
                .arrivalEta(LocalDateTime.now().plusHours(10))
                .status(TripStatus.SCHEDULED)
                .build();

        return tripRepository.save(trip);
    }

    private Ticket createAndSaveTicket() {
        Trip trip = createAndSaveTrip();
        return createTicketForTrip(trip, "default@example.com", "3000000000");
    }

    private Ticket createTicketForTrip(Trip trip, String email, String phone) {
        User passenger = User.builder()
                .username("Test Passenger")
                .email(email)
                .phone(phone)
                .role(UserRole.PASSENGER)
                .passwordHash("hash-de-prueba")
                .build();
        User savedPassenger = userRepository.save(passenger);

        Stop savedFromStop = stopRepository.save(Stop.builder()
                .route(trip.getRoute())
                .name("From Stop")
                .order(1)
                .lat(new BigDecimal("4.0"))
                .lng(new BigDecimal("-74.0"))
                .build());

        Stop savedToStop = stopRepository.save(Stop.builder()
                .route(trip.getRoute())
                .name("To Stop")
                .order(2)
                .lat(new BigDecimal("5.0"))
                .lng(new BigDecimal("-75.0"))
                .build());


        Ticket ticket = Ticket.builder()
                .trip(trip)
                .passenger(savedPassenger)
                .fromStop(savedFromStop)
                .toStop(savedToStop)
                .seatNumber("A" + System.currentTimeMillis() % 100)
                .price(new BigDecimal("50000"))
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.SOLD)
                .qrCode("QR-" + UUID.randomUUID())
                .build();

        return ticketRepository.save(ticket);
    }

    private Baggage createBaggage(Ticket ticket, String weight, String fee, String tagCode) {
        return Baggage.builder()
                .ticket(ticket)
                .weightKg(new BigDecimal(weight))
                .fee(new BigDecimal(fee))
                .tagCode(tagCode)
                .build();
    }
}