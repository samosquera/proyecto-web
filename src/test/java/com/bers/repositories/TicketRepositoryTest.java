package com.bers.repositories;

import com.bers.AbstractRepositoryTest;
import com.bers.domain.entities.*;
import com.bers.domain.entities.enums.*;
import com.bers.domain.repositories.*;
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

class TicketRepositoryTest extends AbstractRepositoryTest {

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
    @DisplayName("Guardar un ticket")
    void shouldSaveTicket() {
        Trip trip = createAndSaveTrip();
        User passenger = createAndSaveUser(
                "passenger@example.com", "3001111111");

        Stop fromStop = createAndSaveStop(
                trip.getRoute(), "Origin", 1);
        Stop toStop = createAndSaveStop(
                trip.getRoute(), "Destination", 2);

        Ticket ticket = createTicket(
                trip, passenger, fromStop, toStop, "A1");

        Ticket saved = ticketRepository.save(ticket);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getQrCode()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(TicketStatus.SOLD);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Buscar ticket por QR")
    void shouldFindTicketByQrCode() {
        Trip trip = createAndSaveTrip();
        User passenger = createAndSaveUser(
                "qr@example.com", "3002222222");
        Stop fromStop = createAndSaveStop(
                trip.getRoute(), "From", 1);
        Stop toStop = createAndSaveStop(
                trip.getRoute(), "To", 2);

        String qrCode = "QR-" + UUID.randomUUID();
        Ticket ticket = createTicket(
                trip, passenger, fromStop, toStop, "B1");
        ticket.setQrCode(qrCode);
        ticketRepository.save(ticket);

        Optional<Ticket> found = ticketRepository.findByQrCode(qrCode);

        assertThat(found).isPresent();
        assertThat(found.get().getSeatNumber()).isEqualTo("B1");
    }

    @Test
    @DisplayName("Buscar lista de ticket por id y estado de trip")
    void shouldFindTicketsByTripIdAndStatus() {
        Trip trip = createAndSaveTrip();
        User passenger = createAndSaveUser(
                "status@example.com", "3003333333");
        Stop fromStop = createAndSaveStop(
                trip.getRoute(), "A", 1);
        Stop toStop = createAndSaveStop(
                trip.getRoute(), "B", 2);

        Ticket sold1 = createTicket(
                trip, passenger, fromStop, toStop, "C1");

        Ticket sold2 = createTicket(
                trip, passenger, fromStop, toStop, "C2");

        Ticket cancelled = createTicket(
                trip, passenger, fromStop, toStop, "C3");
        cancelled.setStatus(TicketStatus.CANCELLED);

        ticketRepository.saveAll(List.of(sold1, sold2, cancelled));

        List<Ticket> soldTickets = ticketRepository.findByTripIdAndStatus(
                trip.getId(), TicketStatus.SOLD);

        assertThat(soldTickets).hasSize(2);
        assertThat(soldTickets).extracting(Ticket::getSeatNumber)
                .containsExactlyInAnyOrder("C1", "C2");
    }

    @Test
    @DisplayName("Buscar los ticket de un pasajero")
    void shouldFindTicketsByPassengerId() {
        Trip trip = createAndSaveTrip();
        User passenger = createAndSaveUser(
                "mytickets@example.com",
                "3004444444");
        Stop fromStop = createAndSaveStop(
                trip.getRoute(), "X", 1);

        Stop toStop = createAndSaveStop(
                trip.getRoute(), "Y", 2);

        ticketRepository.saveAll(List.of(
                createTicket(trip, passenger, fromStop, toStop, "D1"),
                createTicket(trip, passenger, fromStop, toStop, "D2")
        ));

        List<Ticket> tickets = ticketRepository.findByPassengerId(passenger.getId());

        assertThat(tickets).hasSize(2);
    }

    @Test
    @DisplayName("Buscar ticket por id")
    void shouldFindTicketByIdWithDetails() {
        Trip trip = createAndSaveTrip();
        User passenger = createAndSaveUser(
                "details@example.com", "3005555555");

        Stop fromStop = createAndSaveStop(
                trip.getRoute(), "P", 1);

        Stop toStop = createAndSaveStop(
                trip.getRoute(), "Q", 2);

        Ticket ticket = createTicket(
                trip, passenger, fromStop, toStop, "E1");
        Ticket saved = ticketRepository.save(ticket);

        Optional<Ticket> found = ticketRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTrip()).isNotNull();
        assertThat(found.get().getPassenger()).isNotNull();
    }

    @Test
    @DisplayName("Buscar ticket por numero de seat")
    void shouldFindSoldTicketBySeat() {
        Trip trip = createAndSaveTrip();
        User passenger = createAndSaveUser(
                "seat@example.com", "3006666666");
        Stop fromStop = createAndSaveStop(
                trip.getRoute(), "R", 1);

        Stop toStop = createAndSaveStop(
                trip.getRoute(), "S", 2);

        Ticket ticket = createTicket(
                trip, passenger, fromStop, toStop, "F5");
        ticketRepository.save(ticket);

        Optional<Ticket> found = ticketRepository.findSoldTicketBySeat(trip.getId(), "F5");

        assertThat(found).isPresent();
        assertThat(found.get().getSeatNumber()).isEqualTo("F5");
    }

    @Test
    @DisplayName("Buscar la cantidad ticket vendidios para un trip")
    void shouldCountSoldTicketsByTrip() {
        Trip trip = createAndSaveTrip();
        User passenger = createAndSaveUser(
                "count@example.com", "3007777777");

        Stop fromStop = createAndSaveStop(
                trip.getRoute(), "T", 1);

        Stop toStop = createAndSaveStop(
                trip.getRoute(), "U", 2);

        ticketRepository.saveAll(List.of(
                createTicket(
                        trip, passenger, fromStop, toStop, "G1"),
                createTicket(
                        trip, passenger, fromStop, toStop, "G2"),
                createTicket(
                        trip, passenger, fromStop, toStop, "G3")
        ));

        long count = ticketRepository.countSoldTicketsByTrip(trip.getId());

        assertThat(count).isEqualTo(3);
    }
    private Trip createAndSaveTrip() {
        var route = Route.builder()
                .code("ROUTE-0887")
                .name("Test Route")
                .origin("Origin")
                .destination("Destination")
                .distanceKm(100)
                .durationMin(120)
                .build();
        var savedRoute = routeRepository.save(route);

        var bus = Bus.builder()
                .plate("BUS-0123")
                .capacity(40)
                .status(BusStatus.ACTIVE)
                .build();

        var savedBus = busRepository.save(bus);

        var trip = Trip.builder()
                .route(savedRoute)
                .bus(savedBus)
                .date(LocalDate.now())
                .departureAt(LocalDateTime.now().plusHours(2))
                .arrivalEta(LocalDateTime.now().plusHours(10))
                .status(TripStatus.SCHEDULED)
                .build();
        return tripRepository.save(trip);
    }

    private User createAndSaveUser(String email, String phone) {
        var user = User.builder()
                .username("Test User")
                .email(email)
                .phone(phone)
                .role(UserRole.PASSENGER)
                .passwordHash("hash")
                .build();
        return userRepository.save(user);
    }

    private Stop createAndSaveStop(Route route, String name, Integer order) {
        var stop = Stop.builder()
                .route(route)
                .name(name)
                .order(order)
                .lat(new BigDecimal("4.0"))
                .lng(new BigDecimal("-74.0"))
                .build();
        return stopRepository.save(stop);
    }

    private Ticket createTicket(Trip trip, User passenger, Stop fromStop, Stop toStop, String seatNumber) {
        return Ticket.builder()
                .trip(trip)
                .passenger(passenger)
                .fromStop(fromStop)
                .toStop(toStop)
                .seatNumber(seatNumber)
                .price(new BigDecimal("50000"))
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.SOLD)
                .qrCode("QR-" + UUID.randomUUID())
                .build();
    }
}