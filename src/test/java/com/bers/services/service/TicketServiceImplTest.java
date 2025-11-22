package com.bers.services.service;

import com.bers.api.dtos.TicketDtos.*;
import com.bers.domain.entities.*;
import com.bers.domain.entities.enums.*;
import com.bers.domain.repositories.*;
import com.bers.services.mappers.TicketMapper;
import com.bers.services.service.serviceImple.TicketServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TripRepository tripRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StopRepository stopRepository;
    @Mock
    private FareRuleRepository fareRuleRepository;
    @Mock
    private SeatHoldRepository seatHoldRepository;
    @Spy
    private TicketMapper ticketMapper = Mappers.getMapper(TicketMapper.class);
    @InjectMocks
    private TicketServiceImpl ticketService;

    private Ticket ticket;
    private Trip trip;
    private User passenger;
    private Stop fromStop;
    private Stop toStop;
    private TicketCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        fromStop = Stop.builder().id(1L).name("Bogotá").order(0).build();
        toStop = Stop.builder().id(2L).name("Tunja").order(1).build();
        trip = Trip.builder()
                .id(1L)
                .date(LocalDate.now())
                .departureAt(LocalDateTime.now())
                .route(Route.builder().id(1L).build())
                .build();
        passenger = User.builder()
                .id(1L)
                .username("John Doe")
                .build();
        ticket = Ticket.builder()
                .id(1L)
                .trip(trip)
                .passenger(passenger)
                .fromStop(fromStop)
                .toStop(toStop)
                .seatNumber("1A")
                .price(new BigDecimal("50000"))
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.SOLD)
                .qrCode("QR-123")
                .createdAt(LocalDateTime.now())
                .build();
        createRequest = new TicketCreateRequest(
                1L, 1L, 1L, 2L, "1A", PaymentMethod.CASH
        );
    }

    @Test
    @DisplayName("Debe crear un ticket exitosamente")
    void shouldCreateTicketSuccessfully() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
        when(ticketRepository.findSoldTicketBySeat(any(), any())).thenReturn(Optional.empty());
        when(seatHoldRepository.existsByTripIdAndSeatNumberAndStatus(any(), any(), any()))
                .thenReturn(false);
        when(fareRuleRepository.findFareForSegment(any(), any(), any()))
                .thenReturn(Optional.of(FareRule.builder()
                        .basePrice(new BigDecimal("50000")).build()));
        when(ticketRepository.save(any())).thenReturn(ticket);

        TicketResponse result = ticketService.createTicket(createRequest);

        assertNotNull(result);
        Assertions.assertEquals("1A", result.seatNumber());
        verify(ticketRepository).save(any(Ticket.class));
        verify(ticketMapper).toEntity(createRequest);
        verify(ticketMapper).toResponse(any(Ticket.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el seat no está disponible")
    void shouldThrowExceptionWhenSeatNotAvailable() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
        when(ticketRepository.findSoldTicketBySeat(any(), any()))
                .thenReturn(Optional.of(ticket));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.createTicket(createRequest)
        );

        Assertions.assertTrue(exception.getMessage().contains("not available"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción con stop sequence inválida")
    void shouldThrowExceptionWithInvalidStopSequence() {
        Stop invalidStop = Stop.builder().id(3L).order(2).build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(invalidStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.createTicket(createRequest)
        );

        Assertions.assertTrue(exception.getMessage().contains("Invalid stop sequence"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe cancelar un ticket")
    void shouldCancelTicket() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any())).thenReturn(ticket);

        TicketResponse result = ticketService.cancelTicket(1L);

        assertNotNull(result);
        Assertions.assertEquals(TicketStatus.CANCELLED, ticket.getStatus());
        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("Debe marcar ticket como no_show")
    void shouldMarkTicketAsNoShow() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any())).thenReturn(ticket);

        TicketResponse result = ticketService.markAsNoShow(1L);

        assertNotNull(result);
        Assertions.assertEquals(TicketStatus.NO_SHOW, ticket.getStatus());
    }

    @Test
    @DisplayName("Debe verificar si el seat está disponible")
    void shouldVerifyIfSeatIsAvailable() {
        when(ticketRepository.findSoldTicketBySeat(1L, "1A")).thenReturn(Optional.empty());
        when(seatHoldRepository.existsByTripIdAndSeatNumberAndStatus(
                1L, "1A", HoldStatus.HOLD)).thenReturn(false);

        boolean result = ticketService.isSeatAvailable(1L, "1A");

        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("Debe retornar false cuando seat está vendido")
    void shouldReturnFalseWhenSeatIsSold() {
        when(ticketRepository.findSoldTicketBySeat(1L, "1A"))
                .thenReturn(Optional.of(ticket));

        boolean result = ticketService.isSeatAvailable(1L, "1A");

        assertFalse(result);
    }
}