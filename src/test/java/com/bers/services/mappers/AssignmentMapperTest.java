package com.bers.services.mappers;

import com.bers.api.dtos.AssignmentDtos.AssignmentCreateRequest;
import com.bers.api.dtos.AssignmentDtos.AssignmentResponse;
import com.bers.domain.entities.Assignment;
import com.bers.domain.entities.Route;
import com.bers.domain.entities.Trip;
import com.bers.domain.entities.User;
import com.bers.domain.entities.enums.TripStatus;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AssignmentMapper Tests")
class AssignmentMapperTest {
    private AssignmentMapper assignmentMapper;

    @BeforeEach
    void setUp() {
        assignmentMapper = Mappers.getMapper(AssignmentMapper.class);
    }

    @Test
    @DisplayName("Debe mapear AssignmentCreateRequest a la entidad Assignment")
    void shouldMapCreateRequestToEntity() {
        AssignmentCreateRequest request = new AssignmentCreateRequest(
                1L,
                2L,
                3L
        );

        Assignment assignment = assignmentMapper.toEntity(request);

        assertNotNull(assignment);
        assertFalse(assignment.getChecklistOk());
        assertNull(assignment.getId());
    }

    @Test
    @DisplayName("Debe mapear la entidad Assignment a AssignmentResponse")
    void shouldMapEntityToResponse() {
        Route route = Route.builder().id(1L)
                .origin("Bogotá")
                .destination("Tunja")
                .build();

        Trip trip = Trip.builder().id(1L)
                .date(LocalDate.of(2025, 12, 25))
                .departureAt(LocalDateTime.of(2025, 12, 25, 8, 0))
                .status(TripStatus.SCHEDULED)
                .route(route)
                .build();

        User driver = User.builder().id(2L).username("Driver One").build();
        User dispatcher = User.builder().id(3L).username("Dispatcher One").build();
        LocalDateTime assignedAt = LocalDateTime.now();

        Assignment assignment = Assignment.builder()
                .id(1L)
                .trip(trip)
                .driver(driver)
                .dispatcher(dispatcher)
                .checklistOk(true)
                .assignedAt(assignedAt)
                .build();

        AssignmentResponse response = assignmentMapper.toResponse(assignment);

        assertNotNull(response);
        Assertions.assertEquals(1L, response.id());
        Assertions.assertTrue(response.checklistOk());
        Assertions.assertEquals(assignedAt, response.assignedAt());
        Assertions.assertEquals(1L, response.tripId());
        assertNotNull(response.tripInfo());
        Assertions.assertEquals("SCHEDULED", response.tripStatus());
        Assertions.assertEquals(2L, response.driverId());
        Assertions.assertEquals("Driver One", response.driverName());
        Assertions.assertEquals(3L, response.dispatcherId());
        Assertions.assertEquals("Dispatcher One", response.dispatcherName());
    }
}