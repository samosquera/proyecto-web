package com.bers.services.service.serviceImple;

import com.bers.api.dtos.BaggageDtos.BaggageCreateRequest;
import com.bers.api.dtos.BaggageDtos.BaggageResponse;
import com.bers.api.dtos.BaggageDtos.BaggageUpdateRequest;
import com.bers.domain.entities.Baggage;
import com.bers.domain.entities.Ticket;
import com.bers.domain.repositories.BaggageRepository;
import com.bers.domain.repositories.TicketRepository;
import com.bers.services.mappers.BaggageMapper;
import com.bers.services.service.BaggageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BaggageServiceImpl implements BaggageService {

    private static final BigDecimal FREE_WEIGHT_KG = new BigDecimal("20.0");
    private static final BigDecimal PRICE_PER_KG = new BigDecimal("2000");
    private final BaggageRepository baggageRepository;
    private final TicketRepository ticketRepository;
    private final BaggageMapper baggageMapper;

    @Override
    public BaggageResponse createBaggage(BaggageCreateRequest request) {
        Ticket ticket = ticketRepository.findById(request.ticketId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + request.ticketId()));

        Baggage baggage = baggageMapper.toEntity(request);
        baggage.setTicket(ticket);

        BigDecimal fee = calculateBaggageFee(request.weightKg());
        baggage.setFee(fee);

        Baggage savedBaggage = baggageRepository.save(baggage);
        return baggageMapper.toResponse(savedBaggage);
    }

    @Override
    public BaggageResponse updateBaggage(Long id, BaggageUpdateRequest request) {
        Baggage baggage = baggageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Baggage not found: " + id));

        baggageMapper.updateEntity(request, baggage);
        Baggage updatedBaggage = baggageRepository.save(baggage);
        return baggageMapper.toResponse(updatedBaggage);
    }

    @Override
    @Transactional
    public BaggageResponse getBaggageById(Long id) {
        Baggage baggage = baggageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Baggage not found: " + id));
        return baggageMapper.toResponse(baggage);
    }

    @Override
    @Transactional
    public BaggageResponse getBaggageByTagCode(String tagCode) {
        Baggage baggage = baggageRepository.findByTagCode(tagCode)
                .orElseThrow(() -> new IllegalArgumentException("Baggage not found with tag: " + tagCode));
        return baggageMapper.toResponse(baggage);
    }

    @Override
    @Transactional
    public List<BaggageResponse> getAllBaggage() {
        return baggageRepository.findAll().stream()
                .map(baggageMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<BaggageResponse> getBaggageByTicketId(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new IllegalArgumentException("Ticket not found: " + ticketId);
        }
        return baggageRepository.findByTicketId(ticketId).stream()
                .map(baggageMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<BaggageResponse> getBaggageByTripId(Long tripId) {
        return baggageRepository.findByTripId(tripId).stream()
                .map(baggageMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBaggage(Long id) {
        if (!baggageRepository.existsById(id)) {
            throw new IllegalArgumentException("Baggage not found: " + id);
        }
        baggageRepository.deleteById(id);
    }

    @Override
    public BigDecimal calculateBaggageFee(BigDecimal weightKg) {
        if (weightKg.compareTo(FREE_WEIGHT_KG) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal excessWeight = weightKg.subtract(FREE_WEIGHT_KG);
        return excessWeight.multiply(PRICE_PER_KG);
    }

    @Override
    @Transactional
    public BigDecimal getTotalWeightByTrip(Long tripId) {
        BigDecimal totalWeight = baggageRepository.getTotalWeightByTrip(tripId);
        return totalWeight != null ? totalWeight : BigDecimal.ZERO;
    }
}
