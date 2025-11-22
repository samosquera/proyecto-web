package com.bers.services.service.serviceImple;

import com.bers.api.dtos.AssignmentDtos.AssignmentCreateRequest;
import com.bers.api.dtos.AssignmentDtos.AssignmentResponse;
import com.bers.api.dtos.AssignmentDtos.AssignmentUpdateRequest;
import com.bers.domain.entities.Assignment;
import com.bers.domain.entities.Trip;
import com.bers.domain.entities.User;
import com.bers.domain.entities.enums.UserRole;
import com.bers.domain.repositories.AssignmentRepository;
import com.bers.domain.repositories.TripRepository;
import com.bers.domain.repositories.UserRepository;
import com.bers.services.mappers.AssignmentMapper;
import com.bers.services.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final AssignmentMapper assignmentMapper;

    @Override
    public AssignmentResponse createAssignment(AssignmentCreateRequest request) {
        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + request.tripId()));

        User driver = userRepository.findById(request.driverId())
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + request.driverId()));

        User dispatcher = userRepository.findById(request.dispatcherId())
                .orElseThrow(() -> new IllegalArgumentException("Dispatcher not found: " + request.dispatcherId()));

        // Validar roles
        if (driver.getRole() != UserRole.DRIVER) {
            throw new IllegalArgumentException("User " + driver.getId() + " is not a DRIVER");
        }

        if (dispatcher.getRole() != UserRole.DISPATCHER) {
            throw new IllegalArgumentException("User " + dispatcher.getId() + " is not a DISPATCHER");
        }

        // Validar que el trip no tenga ya una asignación
        if (assignmentRepository.existsByTripId(request.tripId())) {
            throw new IllegalArgumentException("Trip already has an assignment");
        }

        Assignment assignment = assignmentMapper.toEntity(request);
        assignment.setTrip(trip);
        assignment.setDriver(driver);
        assignment.setDispatcher(dispatcher);

        Assignment savedAssignment = assignmentRepository.save(assignment);
        return assignmentMapper.toResponse(savedAssignment);
    }

    @Override
    public AssignmentResponse updateAssignment(Long id, AssignmentUpdateRequest request) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + id));

        assignmentMapper.updateEntity(request, assignment);
        Assignment updatedAssignment = assignmentRepository.save(assignment);
        return assignmentMapper.toResponse(updatedAssignment);
    }

    @Override
    @Transactional
    public AssignmentResponse getAssignmentById(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + id));
        return assignmentMapper.toResponse(assignment);
    }

    @Override
    @Transactional
    public AssignmentResponse getAssignmentByTripId(Long tripId) {
        Assignment assignment = assignmentRepository.findByTripId(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found for trip: " + tripId));
        return assignmentMapper.toResponse(assignment);
    }

    @Override
    @Transactional
    public AssignmentResponse getAssignmentWithDetails(Long tripId) {
        Assignment assignment = assignmentRepository.findByTripIdWithDetails(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found for trip: " + tripId));
        return assignmentMapper.toResponse(assignment);
    }

    @Override
    @Transactional
    public List<AssignmentResponse> getAllAssignments() {
        return assignmentRepository.findAll().stream()
                .map(assignmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<AssignmentResponse> getAssignmentsByDriverId(Long driverId) {
        if (!userRepository.existsById(driverId)) {
            throw new IllegalArgumentException("Driver not found: " + driverId);
        }
        return assignmentRepository.findByDriverId(driverId).stream()
                .map(assignmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<AssignmentResponse> getAssignmentsByDispatcherId(Long dispatcherId) {
        if (!userRepository.existsById(dispatcherId)) {
            throw new IllegalArgumentException("Dispatcher not found: " + dispatcherId);
        }
        return assignmentRepository.findByDispatcherId(dispatcherId).stream()
                .map(assignmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<AssignmentResponse> getActiveAssignmentsByDriver(Long driverId) {
        if (!userRepository.existsById(driverId)) {
            throw new IllegalArgumentException("Driver not found: " + driverId);
        }
        return assignmentRepository.findActiveAssignmentsByDriver(driverId).stream()
                .map(assignmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<AssignmentResponse> getAssignmentsByDateRange(LocalDateTime start, LocalDateTime end) {
        return assignmentRepository.findByDepartureDateRange(start, end).stream()
                .map(assignmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAssignment(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Assignment not found: " + id);
        }
        assignmentRepository.deleteById(id);
    }

    @Override
    public AssignmentResponse approveChecklist(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + id));

        assignment.setChecklistOk(true);
        Assignment updatedAssignment = assignmentRepository.save(assignment);
        return assignmentMapper.toResponse(updatedAssignment);
    }

    @Override
    @Transactional
    public List<AssignmentResponse> getAssignmentsByDriverAndDate(Long driverId, LocalDate date) {
        if (!userRepository.existsById(driverId)) {
            throw new IllegalArgumentException("Driver not found: " + driverId);
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        return assignmentRepository.findByDriverIdAndAssignedAtBetween(driverId, start, end).stream()
                .map(assignmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean hasActiveAssignment(Long tripId) {
        return assignmentRepository.existsByTripId(tripId);
    }

}
