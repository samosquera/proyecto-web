package com.bers.services.service;

import com.bers.api.dtos.IncidentDtos.IncidentCreateRequest;
import com.bers.api.dtos.IncidentDtos.IncidentResponse;
import com.bers.api.dtos.IncidentDtos.IncidentUpdateRequest;
import com.bers.domain.entities.enums.EntityType;
import com.bers.domain.entities.enums.IncidentType;

import java.time.LocalDateTime;
import java.util.List;

public interface IncidentService {

    IncidentResponse createIncident(IncidentCreateRequest request);

    IncidentResponse updateIncident(Long id, IncidentUpdateRequest request);

    IncidentResponse getIncidentById(Long id);

    List<IncidentResponse> getAllIncidents();

    List<IncidentResponse> getIncidentsByEntityTypeAndId(EntityType entityType, Long entityId);

    List<IncidentResponse> getIncidentsByType(IncidentType type);

    List<IncidentResponse> getIncidentsByReportedBy(Long reportedById);

    List<IncidentResponse> getIncidentsByDateRange(LocalDateTime start, LocalDateTime end);

    void deleteIncident(Long id);

    long countIncidentsByType(IncidentType type, LocalDateTime since);
}
