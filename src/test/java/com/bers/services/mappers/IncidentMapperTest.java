package com.bers.services.mappers;

import com.bers.api.dtos.IncidentDtos.IncidentCreateRequest;
import com.bers.api.dtos.IncidentDtos.IncidentResponse;
import com.bers.domain.entities.Incident;
import com.bers.domain.entities.User;
import com.bers.domain.entities.enums.EntityType;
import com.bers.domain.entities.enums.IncidentType;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("IncidentMapper Tests")
class IncidentMapperTest {
    private IncidentMapper incidentMapper;

    @BeforeEach
    void setUp() {
        incidentMapper = Mappers.getMapper(IncidentMapper.class);
    }

    @Test
    @DisplayName("Debe mapear IncidentCreateRequest a la entidad Incident")
    void shouldMapCreateRequestToEntity() {
        IncidentCreateRequest request = new IncidentCreateRequest(
                EntityType.TRIP,
                1L,
                IncidentType.VEHICLE,
                "Tire puncture on route",
                2L
        );

        Incident incident = incidentMapper.toEntity(request);

        assertNotNull(incident);
        Assertions.assertEquals(EntityType.TRIP, incident.getEntityType());
        Assertions.assertEquals(1L, incident.getEntityId());
        Assertions.assertEquals(IncidentType.VEHICLE, incident.getType());
        Assertions.assertEquals("Tire puncture on route", incident.getNote());
        assertNull(incident.getId());
    }

    @Test
    @DisplayName("Debe mapear la entidad Incident a IncidentResponse")
    void shouldMapEntityToResponse() {
        User reporter = User.builder().id(2L).username("Reporter User").build();
        LocalDateTime createdAt = LocalDateTime.now();

        Incident incident = Incident.builder()
                .id(1L)
                .entityType(EntityType.PARCEL)
                .entityId(5L)
                .type(IncidentType.DELIVERY_FAIL)
                .note("Receiver not available")
                .reportedBy(reporter)
                .createdAt(createdAt)
                .build();

        IncidentResponse response = incidentMapper.toResponse(incident);

        assertNotNull(response);
        Assertions.assertEquals(1L, response.id());
        Assertions.assertEquals("PARCEL", response.entityType());
        Assertions.assertEquals(5L, response.entityId());
        Assertions.assertEquals("DELIVERY_FAIL", response.type());
        Assertions.assertEquals("Receiver not available", response.note());
        Assertions.assertEquals(2L, response.reportedBy());
        Assertions.assertEquals("Reporter User", response.reportedByName());
        Assertions.assertEquals(createdAt, response.createdAt());
    }

    @Test
    @DisplayName("Debe mapear todos los tipos de IncidentType correctamente")
    void shouldMapAllIncidentTypes() {
        for (IncidentType type : IncidentType.values()) {
            Incident incident = Incident.builder()
                    .id(1L)
                    .entityType(EntityType.TRIP)
                    .entityId(1L)
                    .type(type)
                    .note("Test")
                    .reportedBy(User.builder().id(1L).username("Test").build())
                    .createdAt(LocalDateTime.now())
                    .build();

            IncidentResponse response = incidentMapper.toResponse(incident);

            Assertions.assertEquals(type.name(), response.type());
        }
    }

    @Test
    @DisplayName("Debe mapear todos los tipos de EntityType correctamente")
    void shouldMapAllEntityTypes() {
        for (EntityType entityType : EntityType.values()) {
            Incident incident = Incident.builder()
                    .id(1L)
                    .entityType(entityType)
                    .entityId(1L)
                    .type(IncidentType.OTHER)
                    .note("Test")
                    .reportedBy(User.builder().id(1L).username("Test").build())
                    .createdAt(LocalDateTime.now())
                    .build();

            IncidentResponse response = incidentMapper.toResponse(incident);

            Assertions.assertEquals(entityType.name(), response.entityType());
        }
    }
}