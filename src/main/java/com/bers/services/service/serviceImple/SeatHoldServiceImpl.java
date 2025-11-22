package com.bers.services.service.serviceImple;

import com.bers.api.dtos.SeatHoldDtos.SeatHoldCreateRequest;
import com.bers.api.dtos.SeatHoldDtos.SeatHoldResponse;
import com.bers.api.dtos.SeatHoldDtos.SeatHoldUpdateRequest;
import com.bers.domain.entities.SeatHold;
import com.bers.domain.entities.Stop;
import com.bers.domain.entities.Trip;
import com.bers.domain.entities.User;
import com.bers.domain.entities.enums.HoldStatus;
import com.bers.domain.repositories.SeatHoldRepository;
import com.bers.domain.repositories.StopRepository;
import com.bers.domain.repositories.TripRepository;
import com.bers.domain.repositories.UserRepository;
import com.bers.services.mappers.SeatHoldMapper;
import com.bers.services.service.SeatHoldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SeatHoldServiceImpl implements SeatHoldService {
    private static final int HOLD_DURATION_MINUTES = 10;
    private final SeatHoldRepository seatHoldRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final StopRepository stopRepository;
    private final SeatHoldMapper seatHoldMapper;

    @Override
    public SeatHoldResponse createSeatHold(SeatHoldCreateRequest request, Long userId) {
        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + request.tripId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Stop fromStop = stopRepository.findById(request.fromStopId())
                .orElseThrow(() -> new IllegalArgumentException("From stop not found: " + request.fromStopId()));

        Stop toStop = stopRepository.findById(request.toStopId())
                .orElseThrow(() -> new IllegalArgumentException("To stop not found: " + request.toStopId()));

        if (isSeatHeld(request.tripId(), request.seatNumber())) {
            log.debug("Its not possible to create a hold for the seat {} in the trip {}", request.seatNumber(), trip.getId());
            throw new IllegalArgumentException("Seat " + request.seatNumber() + " is already held");
        }

        SeatHold seatHold = seatHoldMapper.toEntity(request);
        seatHold.setTrip(trip);
        seatHold.setUser(user);
        seatHold.setFromStop(fromStop);
        seatHold.setToStop(toStop);
        seatHold.setExpiresAt(LocalDateTime.now().plusMinutes(HOLD_DURATION_MINUTES));
        seatHold.setStatus(HoldStatus.HOLD);

        SeatHold savedHold = seatHoldRepository.save(seatHold);
        return seatHoldMapper.toResponse(savedHold);
    }

    @Override
    public SeatHoldResponse updateSeatHold(Long id, SeatHoldUpdateRequest request) {
        SeatHold seatHold = seatHoldRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SeatHold not found: " + id));

        seatHoldMapper.updateEntity(request, seatHold);
        SeatHold updatedHold = seatHoldRepository.save(seatHold);
        return seatHoldMapper.toResponse(updatedHold);
    }

    @Override
    @Transactional
    public SeatHoldResponse getSeatHoldById(Long id) {
        SeatHold seatHold = seatHoldRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SeatHold not found: " + id));
        return seatHoldMapper.toResponse(seatHold);
    }

    @Override
    @Transactional
    public List<SeatHoldResponse> getAllSeatHolds() {
        return seatHoldRepository.findAll().stream()
                .map(seatHoldMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<SeatHoldResponse> getSeatHoldsByTripId(Long tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new IllegalArgumentException("Trip not found: " + tripId);
        }
        return seatHoldRepository.findByTripIdAndStatus(tripId, HoldStatus.HOLD).stream()
                .map(seatHoldMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<SeatHoldResponse> getSeatHoldsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        return seatHoldRepository.findByUserIdAndStatus(userId, HoldStatus.HOLD).stream()
                .map(seatHoldMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<SeatHoldResponse> getActiveSeatHoldsByTrip(Long tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new IllegalArgumentException("Trip not found: " + tripId);
        }
        return seatHoldRepository.findByTripIdAndStatus(tripId, HoldStatus.HOLD).stream()
                .map(seatHoldMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSeatHold(Long id) {
        if (!seatHoldRepository.existsById(id)) {
            throw new IllegalArgumentException("SeatHold not found: " + id);
        }
        seatHoldRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void expireOldHolds() {
        LocalDateTime now = LocalDateTime.now();
        int expiredCount = seatHoldRepository.expireOldHolds(now);

        if (expiredCount > 0) {
            log.info("Expired {} holds manually", expiredCount);
        }
    }

    @Override
    @Transactional
    public boolean isSeatHeld(Long tripId, String seatNumber) {
        return seatHoldRepository.existsByTripIdAndSeatNumberAndStatus(
                tripId, seatNumber, HoldStatus.HOLD);
    }

    @Override
    public void releaseSeatHold(Long holdId) {
        SeatHold seatHold = seatHoldRepository.findById(holdId)
                .orElseThrow(() -> new IllegalArgumentException("SeatHold not found: " + holdId));

        if (seatHold.getStatus() != HoldStatus.HOLD) {
            throw new IllegalArgumentException("Can only release active seatHolds");
        }

        seatHold.setStatus(HoldStatus.EXPIRED);
        seatHoldRepository.save(seatHold);
    }

    @Override
    public void convertHoldToTicket(Long holdId) {
        SeatHold seatHold = seatHoldRepository.findById(holdId)
                .orElseThrow(() -> new IllegalArgumentException("SeatHold not found: " + holdId));

        if (seatHold.getStatus() != HoldStatus.HOLD) {
            throw new IllegalArgumentException("Can only convert active holds");
        }

        seatHold.setStatus(HoldStatus.CONVERTED);
        seatHold.setExpiresAt(LocalDateTime.now());
        seatHoldRepository.save(seatHold);
    }
}
