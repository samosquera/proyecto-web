package com.bers.services.service.serviceImple;

import com.bers.api.dtos.IncidentDtos.IncidentCreateRequest;
import com.bers.api.dtos.IncidentDtos.IncidentResponse;
import com.bers.api.dtos.IncidentDtos.IncidentUpdateRequest;
import com.bers.domain.entities.Incident;
import com.bers.domain.entities.User;
import com.bers.domain.entities.enums.EntityType;
import com.bers.domain.entities.enums.IncidentType;
import com.bers.domain.repositories.IncidentRepository;
import com.bers.domain.repositories.UserRepository;
import com.bers.services.mappers.IncidentMapper;
import com.bers.services.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final IncidentMapper incidentMapper;

    @Override
    public IncidentResponse createIncident(IncidentCreateRequest request) {
        User reportedBy = userRepository.findById(request.reportedBy())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.reportedBy()));

        Incident incident = incidentMapper.toEntity(request);
        incident.setReportedBy(reportedBy);

        Incident savedIncident = incidentRepository.save(incident);
        return incidentMapper.toResponse(savedIncident);
    }

    @Override
    public IncidentResponse updateIncident(Long id, IncidentUpdateRequest request) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + id));

        incidentMapper.updateEntity(request, incident);
        Incident updatedIncident = incidentRepository.save(incident);
        return incidentMapper.toResponse(updatedIncident);
    }

    @Override
    @Transactional
    public IncidentResponse getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + id));
        return incidentMapper.toResponse(incident);
    }

    @Override
    @Transactional
    public List<IncidentResponse> getAllIncidents() {
        return incidentRepository.findAll().stream()
                .map(incidentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<IncidentResponse> getIncidentsByEntityTypeAndId(EntityType entityType, Long entityId) {
        return incidentRepository.findByEntityOrderByCreatedAtDesc(entityType, entityId).stream()
                .map(incidentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<IncidentResponse> getIncidentsByType(IncidentType type) {
        return incidentRepository.findByType(type).stream()
                .map(incidentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<IncidentResponse> getIncidentsByReportedBy(Long reportedById) {
        if (!userRepository.existsById(reportedById)) {
            throw new IllegalArgumentException("User not found: " + reportedById);
        }
        return incidentRepository.findByReportedById(reportedById).stream()
                .map(incidentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<IncidentResponse> getIncidentsByDateRange(LocalDateTime start, LocalDateTime end) {
        return incidentRepository.findByDateRange(start, end).stream()
                .map(incidentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteIncident(Long id) {
        if (!incidentRepository.existsById(id)) {
            throw new IllegalArgumentException("Incident not found: " + id);
        }
        incidentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public long countIncidentsByType(IncidentType type, LocalDateTime since) {
        return incidentRepository.countByTypeAndCreatedAtAfter(type, since);
    }
}
