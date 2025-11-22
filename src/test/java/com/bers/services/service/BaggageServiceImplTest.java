package com.bers.services.service;

import com.bers.api.dtos.BaggageDtos.BaggageCreateRequest;
import com.bers.api.dtos.BaggageDtos.BaggageResponse;
import com.bers.domain.entities.Baggage;
import com.bers.domain.entities.Ticket;
import com.bers.domain.repositories.BaggageRepository;
import com.bers.domain.repositories.TicketRepository;
import com.bers.services.mappers.BaggageMapper;
import com.bers.services.service.serviceImple.BaggageServiceImpl;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BaggageService test")
class BaggageServiceImplTest {
    @Mock
    private BaggageRepository baggageRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Spy
    private BaggageMapper baggageMapper = Mappers.getMapper(BaggageMapper.class);
    @InjectMocks
    private BaggageServiceImpl baggageService;
    private Baggage baggage;
    private Ticket ticket;
    private BaggageCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        ticket = Ticket.builder().id(1L).build();
        baggage = Baggage.builder()
                .id(1L)
                .weightKg(new BigDecimal("25.0"))
                .fee(new BigDecimal("10000"))
                .tagCode("BAG-123")
                .ticket(ticket)
                .build();
        createRequest = new BaggageCreateRequest(1L, new BigDecimal("25.0"));
    }

    @Test
    @DisplayName("Debe crear baggage")
    void shouldCreateBaggageSuccessfully() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(baggageRepository.save(any())).thenReturn(baggage);

        BaggageResponse result = baggageService.createBaggage(createRequest);

        assertNotNull(result);
        assertNotNull(result.tagCode());
        Assertions.assertTrue(result.tagCode().startsWith("BAG-"));
        verify(baggageRepository).save(any(Baggage.class));
        verify(baggageMapper).toEntity(createRequest);
        verify(baggageMapper).toResponse(any(Baggage.class));
    }

    @Test
    @DisplayName("Debe calcular baggage fee cero para weight menor a 20kg")
    void shouldCalculateZeroFeeForWeightLessThan20Kg() {
        BigDecimal weight = new BigDecimal("15.0");

        BigDecimal fee = baggageService.calculateBaggageFee(weight);

        Assertions.assertEquals(BigDecimal.ZERO, fee);
    }

    @Test
    @DisplayName("Debe calcular baggage fee para weight mayor a 20kg")
    void shouldCalculateFeeForWeightGreaterThan20Kg() {
        BigDecimal weight = new BigDecimal("25.0");

        BigDecimal fee = baggageService.calculateBaggageFee(weight);

        Assertions.assertEquals(new BigDecimal("10000.0"), fee);
    }

    @Test
    @DisplayName("Debe obtener total weight por trip")
    void shouldGetTotalWeightByTrip() {
        when(baggageRepository.getTotalWeightByTrip(1L))
                .thenReturn(new BigDecimal("150.5"));

        BigDecimal total = baggageService.getTotalWeightByTrip(1L);

        Assertions.assertEquals(new BigDecimal("150.5"), total);
    }
}