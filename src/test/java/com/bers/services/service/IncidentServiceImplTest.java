package com.bers.services.service;

import com.bers.api.dtos.IncidentDtos.IncidentCreateRequest;
import com.bers.api.dtos.IncidentDtos.IncidentResponse;
import com.bers.domain.entities.Incident;
import com.bers.domain.entities.User;
import com.bers.domain.entities.enums.EntityType;
import com.bers.domain.entities.enums.IncidentType;
import com.bers.domain.repositories.IncidentRepository;
import com.bers.domain.repositories.UserRepository;
import com.bers.services.mappers.IncidentMapper;
import com.bers.services.service.serviceImple.IncidentServiceImpl;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IncidentService test")
class IncidentServiceImplTest {
    @Mock
    private IncidentRepository incidentRepository;
    @Mock
    private UserRepository userRepository;
    @Spy
    private IncidentMapper incidentMapper = Mappers.getMapper(IncidentMapper.class);
    @InjectMocks
    private IncidentServiceImpl incidentService;
    private Incident incident;
    private User reporter;
    private IncidentCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        reporter = User.builder()
                .id(1L)
                .username("Reporter")
                .build();
        incident = Incident.builder()
                .id(1L)
                .entityType(EntityType.TRIP)
                .entityId(1L)
                .type(IncidentType.VEHICLE)
                .note("Tire issue")
                .reportedBy(reporter)
                .createdAt(LocalDateTime.now())
                .build();
        createRequest = new IncidentCreateRequest(
                EntityType.TRIP, 1L, IncidentType.VEHICLE,
                "Tire issue", 1L
        );
    }

    @Test
    @DisplayName("Debe create incident")
    void shouldCreateIncidentSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(incidentRepository.save(any())).thenReturn(incident);

        IncidentResponse result = incidentService.createIncident(createRequest);

        assertNotNull(result);
        Assertions.assertEquals("Tire issue", result.note());
        verify(incidentRepository).save(any(Incident.class));
        verify(incidentMapper).toEntity(createRequest);
        verify(incidentMapper).toResponse(any(Incident.class));
    }

    @Test
    @DisplayName("Debe get incidents por entity type")
    void shouldGetIncidentsByEntityType() {
        List<Incident> incidents = Arrays.asList(incident);
        when(incidentRepository.findByEntityOrderByCreatedAtDesc(
                EntityType.TRIP, 1L)).thenReturn(incidents);

        List<IncidentResponse> result = incidentService
                .getIncidentsByEntityTypeAndId(EntityType.TRIP, 1L);

        assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        verify(incidentMapper, times(1)).toResponse(any(Incident.class));
    }

    @Test
    @DisplayName("Debe count incidents por type")
    void shouldCountIncidentsByType() {
        when(incidentRepository.countByTypeAndCreatedAtAfter(
                any(), any())).thenReturn(5L);

        long count = incidentService.countIncidentsByType(
                IncidentType.VEHICLE, LocalDateTime.now().minusDays(7)
        );

        Assertions.assertEquals(5L, count);
    }
}