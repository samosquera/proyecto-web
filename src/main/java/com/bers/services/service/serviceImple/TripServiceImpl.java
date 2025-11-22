package com.bers.services.service.serviceImple;

import com.bers.api.dtos.TripDtos.TripCreateRequest;
import com.bers.api.dtos.TripDtos.TripResponse;
import com.bers.api.dtos.TripDtos.TripUpdateRequest;
import com.bers.domain.entities.Bus;
import com.bers.domain.entities.Route;
import com.bers.domain.entities.Trip;
import com.bers.domain.entities.enums.TripStatus;
import com.bers.domain.repositories.BusRepository;
import com.bers.domain.repositories.RouteRepository;
import com.bers.domain.repositories.TripRepository;
import com.bers.services.mappers.TripMapper;
import com.bers.services.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    private final TripMapper tripMapper;

    @Override
    public TripResponse createTrip(TripCreateRequest request) {
        Route route = routeRepository.findById(request.routeId())
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + request.routeId()));

        if (request.busId() != null) {
            Bus bus = busRepository.findById(request.busId())
                    .orElseThrow(() -> new IllegalArgumentException("Bus not found: " + request.busId()));
            validateTripSchedule(request.busId(), request.date(), request.departureAt());
        }

        if (request.arrivalEta().isBefore(request.departureAt())) {
            throw new IllegalArgumentException("Arrival time must be after departure time");
        }

        Trip trip = tripMapper.toEntity(request);
        trip.setRoute(route);
        Optional<Bus> bus = busRepository.findById(trip.getBus().getId());
        trip.setBus(bus.get());

        Trip savedTrip = tripRepository.save(trip);
        return tripMapper.toResponse(savedTrip);
    }

    @Override
    public TripResponse updateTrip(Long id, TripUpdateRequest request) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + id));

        if (request.busId() != null && !request.busId().equals(trip.getBus().getId())) {
            busRepository.findById(request.busId())
                    .orElseThrow(() -> new IllegalArgumentException("Bus not found: " + request.busId()));
            validateTripSchedule(request.busId(), trip.getDate(), request.departureAt());
        }

        if (request.arrivalEta().isBefore(request.departureAt())) {
            throw new IllegalArgumentException("Arrival time must be after departure time");
        }

        tripMapper.updateEntity(request, trip);
        Trip updatedTrip = tripRepository.save(trip);
        return tripMapper.toResponse(updatedTrip);
    }

    @Override
    @Transactional
    public TripResponse getTripById(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + id));
        return tripMapper.toResponse(trip);
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponse getTripWithDetails(Long id) {
        Trip trip = tripRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + id));
        return tripMapper.toResponse(trip);
    }

    @Override
    @Transactional
    public List<TripResponse> getAllTrips() {
        return tripRepository.findAll().stream()
                .map(tripMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<TripResponse> getTripsByRouteAndDate(Long routeId, LocalDate date) {
        if (!routeRepository.existsById(routeId)) {
            throw new IllegalArgumentException("Route not found: " + routeId);
        }
        return tripRepository.findByRouteIdAndDate(routeId, date).stream()
                .map(tripMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<TripResponse> getTripsByRouteAndDateAndStatus(Long routeId, LocalDate date, TripStatus status) {
        if (!routeRepository.existsById(routeId)) {
            throw new IllegalArgumentException("Route not found: " + routeId);
        }
        return tripRepository.findByRouteIdAndDateAndStatus(routeId, date, status).stream()
                .map(tripMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<TripResponse> getTripsByDateAndStatus(LocalDate date, TripStatus status) {
        return tripRepository.findByDateAndStatus(date, status).stream()
                .map(tripMapper::toResponse)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public List<TripResponse> searchTrips(Long routeId, LocalDate date, TripStatus status) {
        if (routeId != null && date != null && status != null) {
            return getTripsByRouteAndDateAndStatus(routeId, date, status);
        } else if (routeId != null && date != null) {
            return getTripsByRouteAndDate(routeId, date);
        } else if (date != null && status != null) {
            return getTripsByDateAndStatus(date, status);
        } else if (date != null) {  // ← NUEVA CONDICIÓN AGREGADA
            return getTripsByDate(date);
        }
        return getAllTrips();
    }


    @Override
    @Transactional
    public List<TripResponse> getActiveTripsByBus(Long busId, LocalDate date) {
        if (!busRepository.existsById(busId)) {
            throw new IllegalArgumentException("Bus not found: " + busId);
        }
        return tripRepository.findActiveTripsByBusAndDate(busId, date).stream()
                .map(tripMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTrip(Long id) {
        if (!tripRepository.existsById(id)) {
            throw new IllegalArgumentException("Trip not found: " + id);
        }
        tripRepository.deleteById(id);
    }

    @Override
    @Transactional
    public List<TripResponse> getTripsByDate(LocalDate date) {
        return tripRepository.findByDate(date).stream()
                .map(tripMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TripResponse> getTripsByStatus(TripStatus status) {
        return tripRepository.finByStatus(status).stream()
                .map(tripMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<TripResponse> getTodayActiveTrips() {
        return tripRepository
                .findTodayActiveTrips().stream()
                .map(tripMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TripResponse> filterTripByOriginAndDestination(String origin, String destination, LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Date must be at least today");
        }
        return tripRepository.searchTrips(origin, destination, date).stream()
                .map(tripMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public TripResponse changeTripStatus(Long id, TripStatus status) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + id));

        validateTripStatusTransition(trip.getStatus(), status);

        trip.setStatus(status);
        Trip updatedTrip = tripRepository.save(trip);
        return tripMapper.toResponse(updatedTrip);
    }

    @Override
    @Transactional
    public void validateTripSchedule(Long busId, LocalDate date, LocalDateTime departureAt) {
        List<Trip> activeTrips = tripRepository.findActiveTripsByBusAndDate(busId, date);

        for (Trip existingTrip : activeTrips) {
            if (departureAt.isAfter(existingTrip.getDepartureAt().minusHours(1)) &&
                    departureAt.isBefore(existingTrip.getArrivalEta().plusHours(1))) {
                throw new IllegalArgumentException(
                        "Bus is already scheduled for another trip at this time");
            }
        }
    }

    private void validateTripStatusTransition(TripStatus currentStatus, TripStatus newStatus) {
        if (currentStatus == TripStatus.CANCELLED || currentStatus == TripStatus.ARRIVED) {
            throw new IllegalArgumentException("Cannot change status from " + currentStatus);
        }

        switch (currentStatus) {
            case SCHEDULED:
                if (newStatus != TripStatus.BOARDING && newStatus != TripStatus.CANCELLED) {
                    throw new IllegalArgumentException("Invalid status transition from SCHEDULED to " + newStatus);
                }
                break;
            case BOARDING:
                if (newStatus != TripStatus.DEPARTED && newStatus != TripStatus.CANCELLED) {
                    throw new IllegalArgumentException("Invalid status transition from BOARDING to " + newStatus);
                }
                break;
            case DEPARTED:
                if (newStatus != TripStatus.ARRIVED) {
                    throw new IllegalArgumentException("Invalid status transition from DEPARTED to " + newStatus);
                }
                break;
        }
    }


}
