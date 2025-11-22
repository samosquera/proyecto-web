package com.bers.services.service;

import com.bers.api.dtos.AssignmentDtos.AssignmentCreateRequest;
import com.bers.api.dtos.AssignmentDtos.AssignmentResponse;
import com.bers.api.dtos.AssignmentDtos.AssignmentUpdateRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AssignmentService {

    AssignmentResponse createAssignment(AssignmentCreateRequest request);

    AssignmentResponse updateAssignment(Long id, AssignmentUpdateRequest request);

    List<AssignmentResponse> getAssignmentsByDriverAndDate(Long driverId, LocalDate date);

    AssignmentResponse getAssignmentById(Long id);

    AssignmentResponse getAssignmentByTripId(Long tripId);

    AssignmentResponse getAssignmentWithDetails(Long tripId);

    List<AssignmentResponse> getAllAssignments();

    List<AssignmentResponse> getAssignmentsByDriverId(Long driverId);

    List<AssignmentResponse> getAssignmentsByDispatcherId(Long dispatcherId);

    List<AssignmentResponse> getActiveAssignmentsByDriver(Long driverId);

    List<AssignmentResponse> getAssignmentsByDateRange(LocalDateTime start, LocalDateTime end);

    void deleteAssignment(Long id);

    AssignmentResponse approveChecklist(Long id);

    boolean hasActiveAssignment(Long tripId);
}
