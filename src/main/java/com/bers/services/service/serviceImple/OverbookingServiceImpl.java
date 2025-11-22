package com.bers.services.service.serviceImple;

import com.bers.api.dtos.OverbookingDtos.OverbookingApproveRequest;
import com.bers.api.dtos.OverbookingDtos.OverbookingCreateRequest;
import com.bers.api.dtos.OverbookingDtos.OverbookingRejectRequest;
import com.bers.api.dtos.OverbookingDtos.OverbookingResponse;
import com.bers.domain.entities.*;
import com.bers.domain.entities.enums.OverbookingStatus;
import com.bers.domain.entities.enums.TripStatus;
import com.bers.domain.entities.enums.UserRole;
import com.bers.domain.repositories.OverbookingRequestRepository;
import com.bers.domain.repositories.TicketRepository;
import com.bers.domain.repositories.TripRepository;
import com.bers.domain.repositories.UserRepository;
import com.bers.services.service.ConfigService;
import com.bers.services.service.OverbookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OverbookingServiceImpl implements OverbookingService {

    private static final int OVERBOOKING_APPROVAL_WINDOW_MINUTES = 30;
    private static final double OCCUPANCY_THRESHOLD = 0.95;
    private final OverbookingRequestRepository overbookingRepository;
    private final TripRepository tripRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ConfigService configService;

    @Override
    @Transactional
    public OverbookingResponse requestOverbooking(OverbookingCreateRequest request, Long userId) {
        log.info("Overbooking request for trip: {}, ticket: {}, user: {}",
                request.tripId(), request.ticketId(), userId);

        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + request.tripId()));

        Ticket ticket = ticketRepository.findById(request.ticketId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + request.ticketId()));

        User requestedBy = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        //  CRITERIO 3: Validar condiciones para overbooking
        validateOverbookingConditions(trip);

        if (overbookingRepository.findByTicketId(request.ticketId()).isPresent()) {
            throw new IllegalArgumentException("Overbooking request already exists for this ticket");
        }

        OverbookingRequest overbooking = OverbookingRequest.builder()
                .trip(trip)
                .ticket(ticket)
                .requestedBy(requestedBy)
                .status(OverbookingStatus.PENDING_APPROVAL)
                .reason(request.reason())
                .requestedAt(LocalDateTime.now())
                .expiresAt(trip.getDepartureAt().minusMinutes(5))
                .build();

        OverbookingRequest saved = overbookingRepository.save(overbooking);
        log.info("Overbooking request created: {}", saved.getId());

        return mapToResponse(saved);
    }

    private void validateOverbookingConditions(Trip trip) {
        if (trip.getStatus() != TripStatus.SCHEDULED && trip.getStatus() != TripStatus.BOARDING) {
            throw new IllegalArgumentException("Overbooking only allowed for SCHEDULED or BOARDING trips");
        }

        long minutesUntilDeparture = java.time.Duration.between(LocalDateTime.now(), trip.getDepartureAt()).toMinutes();
        if (minutesUntilDeparture > OVERBOOKING_APPROVAL_WINDOW_MINUTES) {
            throw new IllegalArgumentException("Overbooking only allowed within " +
                    OVERBOOKING_APPROVAL_WINDOW_MINUTES + " minutes of departure");
        }

        double occupancyRate = getCurrentOccupancyRate(trip.getId());
        if (occupancyRate < OCCUPANCY_THRESHOLD) {
            throw new IllegalArgumentException("Overbooking only allowed when occupancy exceeds " +
                    (OCCUPANCY_THRESHOLD * 100) + "%");
        }

        if (!canOverbook(trip.getId())) {
            throw new IllegalArgumentException("Overbooking limit reached for this trip");
        }
    }

    @Override
    @Transactional
    public OverbookingResponse approveOverbooking(Long requestId, Long dispatcherId, OverbookingApproveRequest approveRequest) {
        log.info("Approving overbooking request: {} by dispatcher: {}", requestId, dispatcherId);

        OverbookingRequest request = overbookingRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Overbooking request not found: " + requestId));

        User dispatcher = userRepository.findById(dispatcherId)
                .orElseThrow(() -> new IllegalArgumentException("Dispatcher not found: " + dispatcherId));

        if (dispatcher.getRole() != UserRole.DISPATCHER) {
            throw new IllegalArgumentException("Only DISPATCHER can approve overbooking requests");
        }

        if (request.getStatus() != OverbookingStatus.PENDING_APPROVAL) {
            throw new IllegalArgumentException("Only PENDING_APPROVAL requests can be approved");
        }

        if (request.getExpiresAt().isBefore(LocalDateTime.now())) {
            request.setStatus(OverbookingStatus.EXPIRED);
            overbookingRepository.save(request);
            throw new IllegalArgumentException("Overbooking request has expired");
        }

        request.setStatus(OverbookingStatus.APPROVED);
        request.setApprovedBy(dispatcher);
        request.setApprovedAt(LocalDateTime.now());

        OverbookingRequest approved = overbookingRepository.save(request);
        log.info("Overbooking request approved: {} for trip {}", requestId, request.getTrip().getId());

        return mapToResponse(approved);
    }

    @Override
    @Transactional
    public OverbookingResponse rejectOverbooking(Long requestId, Long dispatcherId, OverbookingRejectRequest rejectRequest) {
        log.info("Rejecting overbooking request: {} by dispatcher: {}", requestId, dispatcherId);

        OverbookingRequest request = overbookingRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Overbooking request not found: " + requestId));

        User dispatcher = userRepository.findById(dispatcherId)
                .orElseThrow(() -> new IllegalArgumentException("Dispatcher not found: " + dispatcherId));

        if (dispatcher.getRole() != UserRole.DISPATCHER) {
            throw new IllegalArgumentException("Only DISPATCHER can reject overbooking requests");
        }

        if (request.getStatus() != OverbookingStatus.PENDING_APPROVAL) {
            throw new IllegalArgumentException("Only PENDING_APPROVAL requests can be rejected");
        }

        request.setStatus(OverbookingStatus.REJECTED);
        request.setApprovedBy(dispatcher);
        request.setApprovedAt(LocalDateTime.now());
        request.setReason(request.getReason() + " - Rejected: " + rejectRequest.reason());

        OverbookingRequest rejected = overbookingRepository.save(request);
        log.info("Overbooking request rejected: {}", requestId);

        return mapToResponse(rejected);
    }

    @Override
    @Transactional
    public List<OverbookingResponse> getPendingRequests() {
        return overbookingRepository.findByStatus(OverbookingStatus.PENDING_APPROVAL)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<OverbookingResponse> getOverbookingRequestsByTrip(Long tripId) {
        return overbookingRepository.findByTripId(tripId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<OverbookingResponse> getOverbookingRequestsByStatus(String status) {
        try {
            OverbookingStatus overbookingStatus = OverbookingStatus.valueOf(status.toUpperCase());
            return overbookingRepository.findByStatus(overbookingStatus)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid overbooking status: " + status);
        }
    }

    @Override
    @Transactional
    public OverbookingResponse getOverbookingRequestById(Long id) {
        OverbookingRequest request = overbookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Overbooking request not found: " + id));
        return mapToResponse(request);
    }

    @Override
    @Transactional
    public boolean canOverbook(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + tripId));

        Bus bus = trip.getBus();
        if (bus == null) {
            throw new IllegalArgumentException("No bus assigned to trip");
        }

        int capacity = bus.getCapacity();
        double maxOverbookingPercentage = configService.getConfigValueAsDouble(
                "overbooking.max.percentage", 5.0);

        long soldTickets = ticketRepository.countSoldTicketsByTrip(tripId);
        long approvedOverbookings = overbookingRepository.countApprovedOverbookingsByTrip(tripId);

        int totalOccupancy = (int) (soldTickets + approvedOverbookings);
        int maxAllowed = capacity + (int) (capacity * maxOverbookingPercentage / 100.0);

        return totalOccupancy < maxAllowed;
    }

    @Override
    @Transactional
    public double getCurrentOccupancyRate(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + tripId));

        Bus bus = trip.getBus();
        if (bus == null) return 0.0;

        int capacity = bus.getCapacity();
        long soldTickets = ticketRepository.countSoldTicketsByTrip(tripId);

        return capacity > 0 ? (double) soldTickets / capacity : 0.0;
    }

    @Override
    @Transactional
    public void expirePendingRequests() {
        LocalDateTime now = LocalDateTime.now();
        List<OverbookingRequest> expiredRequests = overbookingRepository.findExpiredPendingRequests(now);

        for (OverbookingRequest request : expiredRequests) {
            request.setStatus(OverbookingStatus.EXPIRED);
            log.info("Overbooking request expired: {}", request.getId());
        }

        overbookingRepository.saveAll(expiredRequests);
    }

    private OverbookingResponse mapToResponse(OverbookingRequest entity) {
        Trip trip = entity.getTrip();
        Ticket ticket = entity.getTicket();

        double occupancyRate = getCurrentOccupancyRate(trip.getId());
        long minutesUntilDeparture = java.time.Duration.between(LocalDateTime.now(), trip.getDepartureAt()).toMinutes();

        return new OverbookingResponse(
                entity.getId(),
                trip.getId(),
                trip.getRoute().getOrigin() + " → " + trip.getRoute().getDestination() + " - " + trip.getDate(),
                ticket.getId(),
                ticket.getPassenger().getUsername(),
                ticket.getSeatNumber(),
                entity.getStatus().name(),
                entity.getReason(),
                entity.getRequestedBy() != null ? entity.getRequestedBy().getUsername() : null,
                entity.getApprovedBy() != null ? entity.getApprovedBy().getUsername() : null,
                entity.getRequestedAt(),
                entity.getApprovedAt(),
                entity.getExpiresAt(),
                true,
                occupancyRate,
                (int) minutesUntilDeparture
        );
    }
}