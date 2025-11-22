package com.bers.services.service;

import com.bers.api.dtos.AssignmentDtos.AssignmentCreateRequest;
import com.bers.api.dtos.AssignmentDtos.AssignmentResponse;
import com.bers.domain.entities.Assignment;
import com.bers.domain.entities.Route;
import com.bers.domain.entities.Trip;
import com.bers.domain.entities.User;
import com.bers.domain.entities.enums.TripStatus;
import com.bers.domain.entities.enums.UserRole;
import com.bers.domain.repositories.AssignmentRepository;
import com.bers.domain.repositories.TripRepository;
import com.bers.domain.repositories.UserRepository;
import com.bers.services.mappers.AssignmentMapper;
import com.bers.services.service.serviceImple.AssignmentServiceImpl;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssignmentService test")

class AssignmentServiceImplTest {
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private TripRepository tripRepository;
    @Mock
    private UserRepository userRepository;
    @Spy
    private AssignmentMapper assignmentMapper = Mappers.getMapper(AssignmentMapper.class);
    @InjectMocks
    private AssignmentServiceImpl assignmentService;
    private Assignment assignment;
    private Trip trip;
    private User driver;
    private User dispatcher;
    private AssignmentCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        trip = Trip.builder()
                .id(1L)
                .date(LocalDate.now())
                .departureAt(LocalDateTime.now())
                .status(TripStatus.SCHEDULED)
                .route(Route.builder().id(1L).name("Test").build())
                .build();
        driver = User.builder()
                .id(1L)
                .username("Driver")
                .role(UserRole.DRIVER)
                .build();
        dispatcher = User.builder()
                .id(2L)
                .username("Dispatcher")
                .role(UserRole.DISPATCHER)
                .build();
        assignment = Assignment.builder()
                .id(1L)
                .trip(trip)
                .driver(driver)
                .dispatcher(dispatcher)
                .checklistOk(false)
                .assignedAt(LocalDateTime.now())
                .build();
        createRequest = new AssignmentCreateRequest(1L, 1L, 2L);
    }

    @Test
    @DisplayName("Debe crear assignment exitosamente")
    void shouldCreateAssignmentSuccessfully() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(userRepository.findById(2L)).thenReturn(Optional.of(dispatcher));
        when(assignmentRepository.existsByTripId(1L)).thenReturn(false);
        when(assignmentRepository.save(any())).thenReturn(assignment);

        AssignmentResponse result = assignmentService.createAssignment(createRequest);

        assertNotNull(result);
        verify(assignmentRepository).save(any(Assignment.class));
        verify(assignmentMapper).toEntity(createRequest);
        verify(assignmentMapper).toResponse(any(Assignment.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el user no es DRIVER role")
    void shouldThrowExceptionWhenUserIsNotDriverRole() {
        driver.setRole(UserRole.PASSENGER);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(userRepository.findById(2L)).thenReturn(Optional.of(dispatcher));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> assignmentService.createAssignment(createRequest)
        );

        Assertions.assertTrue(exception.getMessage().contains("not a DRIVER"));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el user no es DISPATCHER role")
    void shouldThrowExceptionWhenUserIsNotDispatcherRole() {
        dispatcher.setRole(UserRole.PASSENGER);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(userRepository.findById(2L)).thenReturn(Optional.of(dispatcher));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> assignmentService.createAssignment(createRequest)
        );

        Assertions.assertTrue(exception.getMessage().contains("not a DISPATCHER"));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el trip ya tiene un assignment")
    void shouldThrowExceptionWhenTripAlreadyHasAssignment() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(userRepository.findById(2L)).thenReturn(Optional.of(dispatcher));
        when(assignmentRepository.existsByTripId(1L)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> assignmentService.createAssignment(createRequest)
        );

        Assertions.assertTrue(exception.getMessage().contains("already has an assignment"));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe approve checklist")
    void shouldApproveChecklist() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any())).thenReturn(assignment);

        AssignmentResponse result = assignmentService.approveChecklist(1L);

        assertNotNull(result);
        Assertions.assertTrue(assignment.getChecklistOk());
        verify(assignmentRepository).save(assignment);
    }

    @Test
    @DisplayName("Debe verificar si tiene assignment active")
    void shouldVerifyIfHasActiveAssignment() {
        when(assignmentRepository.existsByTripId(1L)).thenReturn(true);

        boolean result = assignmentService.hasActiveAssignment(1L);

        Assertions.assertTrue(result);
    }
}