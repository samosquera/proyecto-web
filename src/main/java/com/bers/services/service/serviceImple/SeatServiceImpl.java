package com.bers.services.service.serviceImple;

import com.bers.api.dtos.SeatDtos.*;
import com.bers.domain.entities.*;
import com.bers.domain.entities.enums.SeatType;
import com.bers.domain.entities.enums.TicketStatus;
import com.bers.domain.repositories.*;
import com.bers.services.mappers.SeatMapper;
import com.bers.services.service.SeatService;
import com.bers.services.service.SegmentValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final BusRepository busRepository;
    private final SeatMapper seatMapper;
    private final TripRepository tripRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final SegmentValidationService segmentValidationService;
    private final StopRepository stopRepository;
    private final TicketRepository ticketRepository;

    @Override
    public SeatResponse createSeat(SeatCreateRequest request) {
        Bus bus = busRepository.findById(request.busId())
                .orElseThrow(() -> new IllegalArgumentException("Bus not found: " + request.busId()));

        validateSeatNumber(request.busId(), request.number());

        long currentSeats = seatRepository.countByBusId(request.busId());
        if (currentSeats >= bus.getCapacity()) {
            throw new IllegalArgumentException("Bus capacity exceeded. Max capacity: " + bus.getCapacity());
        }

        Seat seat = seatMapper.toEntity(request);
        seat.setBus(bus);

        Seat savedSeat = seatRepository.save(seat);
        return seatMapper.toResponse(savedSeat);
    }

    @Override
    public SeatResponse updateSeat(Long id, SeatUpdateRequest request) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found: " + id));

        seatMapper.updateEntity(request, seat);
        Seat updatedSeat = seatRepository.save(seat);
        return seatMapper.toResponse(updatedSeat);
    }

    @Override
    @Transactional
    public SeatResponse getSeatById(Long id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found: " + id));
        return seatMapper.toResponse(seat);
    }

    @Override
    @Transactional
    public SeatResponse getSeatByBusAndNumber(Long busId, String number) {
        Seat seat = seatRepository.findByBusIdAndNumber(busId, number)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Seat not found with number " + number + " in bus " + busId));
        return seatMapper.toResponse(seat);
    }

    @Override
    @Transactional
    public List<SeatResponse> getAllSeats() {
        return seatRepository.findAll().stream()
                .map(seatMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<SeatResponse> getSeatsByBusId(Long busId) {
        if (!busRepository.existsById(busId)) {
            throw new IllegalArgumentException("Bus not found: " + busId);
        }
        return seatRepository.findByBusIdOrderByNumberAsc(busId).stream()
                .map(seatMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<SeatResponse> getSeatsByBusIdAndType(Long busId, SeatType type) {
        if (!busRepository.existsById(busId)) {
            throw new IllegalArgumentException("Bus not found: " + busId);
        }
        return seatRepository.findByBusIdAndType(busId, type).stream()
                .map(seatMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeatStatusResponse> getAllTripSeatsClassified(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));

        return tripRepository.findSeatsByTripId(tripId).stream()
                .map(seat -> seatMapper.toStatusResponse(seat, tripId, seatHoldRepository))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSeat(Long id) {
        if (!seatRepository.existsById(id)) {
            throw new IllegalArgumentException("Seat not found: " + id);
        }
        seatRepository.deleteById(id);
    }


    @Override
    @Transactional
    public long countSeatsByBus(Long busId) {
        return seatRepository.countByBusId(busId);
    }

    @Override
    @Transactional
    public void validateSeatNumber(Long busId, String number) {
        if (seatRepository.findByBusIdAndNumber(busId, number).isPresent()) {
            throw new IllegalArgumentException(
                    "Seat number " + number + " already exists in bus " + busId);
        }
    }

    @Override
    @Transactional
    public SeatStatusBySegmentResponse getTripSeatsForSegment(
            Long tripId,
            Long fromStopId,
            Long toStopId) {

        // Validar el viaje
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));

        // Obtener todos los asientos del bus
        List<Seat> allSeats = tripRepository.findSeatsByTripId(tripId);

        // Obtener solo los tickets VENDIDOS de este segmento específico
        List<Ticket> segmentTickets = ticketRepository
                .findByTripIdAndFromStopIdAndToStopIdAndStatus(
                        tripId, fromStopId, toStopId, TicketStatus.SOLD
                );

        // Obtener números de asientos ocupados con tickets vendidos
        Set<String> occupiedSeatNumbers = segmentTickets.stream()
                .map(Ticket::getSeatNumber)
                .collect(Collectors.toSet());

        // Obtener holds activos para este segmento específico
        List<SeatHold> activeHolds = seatHoldRepository
                .findActiveHoldsByTripAndSegment(tripId, fromStopId, toStopId, LocalDateTime.now());

        // Obtener números de asientos en hold activo
        Set<String> heldSeatNumbers = activeHolds.stream()
                .map(SeatHold::getSeatNumber)
                .collect(Collectors.toSet());

        // Clasificar todos los asientos con los tres estados
        List<SeatStatusResponse> seatStatuses = allSeats.stream()
                .map(seat -> {
                    boolean isOccupied = occupiedSeatNumbers.contains(seat.getNumber());
                    boolean isHeld = !isOccupied && heldSeatNumbers.contains(seat.getNumber());
                    boolean available = !isOccupied && !isHeld;
                    return new SeatStatusResponse(
                            seat.getId(),
                            seat.getNumber(),
                            isOccupied,
                            isHeld,
                            available
                    );
                })
                .collect(Collectors.toList());

        // Obtener información del segmento
        Stop fromStop = stopRepository.findById(fromStopId)
                .orElseThrow(() -> new IllegalArgumentException("From stop not found"));
        Stop toStop = stopRepository.findById(toStopId)
                .orElseThrow(() -> new IllegalArgumentException("To stop not found"));

        SegmentInfo segmentInfo = new SegmentInfo(
                fromStop.getId(),
                fromStop.getName(),
                fromStop.getOrder(),
                toStop.getId(),
                toStop.getName(),
                toStop.getOrder()
        );

        return new SeatStatusBySegmentResponse(segmentInfo, seatStatuses);
    }


}
