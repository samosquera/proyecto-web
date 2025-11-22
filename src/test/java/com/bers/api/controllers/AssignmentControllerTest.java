package com.bers.api.controllers;

import com.bers.api.dtos.AssignmentDtos.*;
import com.bers.security.config.JwtService;
import com.bers.services.service.AssignmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssignmentController.class)
class AssignmentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AssignmentService assignmentService;
    @MockitoBean
    private JwtService jwtService;
    private AssignmentResponse assignmentResponse;
    private AssignmentCreateRequest createRequest;
    @BeforeEach
    void setUp() {
        assignmentResponse = new AssignmentResponse(
                1L,
                false,
                LocalDateTime.now(),
                1L,
                "Bogotá - Tunja 08:00",
                "SCHEDULED",
                LocalDate.now().plusDays(1),
                LocalDateTime.now().plusDays(1).withHour(8),
                "BOG-TUN",
                1L,
                "Driver John",
                1L,
                "Dispatcher Mary"
        );

        createRequest = new AssignmentCreateRequest(1L, 1L, 1L);
    }

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void createAssignment_ShouldCreateAndReturnAssignment() throws Exception {
        when(assignmentService.createAssignment(any(AssignmentCreateRequest.class)))
                .thenReturn(assignmentResponse);

        mockMvc.perform(post("/api/v1/assignments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tripId").value(1));

        verify(assignmentService).createAssignment(any(AssignmentCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void getAllAssignments_ShouldReturnAssignmentList() throws Exception {
        List<AssignmentResponse> assignments = Arrays.asList(assignmentResponse);
        when(assignmentService.getAllAssignments()).thenReturn(assignments);

        mockMvc.perform(get("/api/v1/assignments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(assignmentService).getAllAssignments();
    }

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void getAssignmentById_ShouldReturnAssignment() throws Exception {
        when(assignmentService.getAssignmentById(1L)).thenReturn(assignmentResponse);

        mockMvc.perform(get("/api/v1/assignments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(assignmentService).getAssignmentById(1L);
    }

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void updateAssignment_ShouldUpdateAndReturnAssignment() throws Exception {
        AssignmentUpdateRequest updateRequest = new AssignmentUpdateRequest(true);

        when(assignmentService.updateAssignment(eq(1L), any(AssignmentUpdateRequest.class)))
                .thenReturn(assignmentResponse);

        mockMvc.perform(put("/api/v1/assignments/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(assignmentService).updateAssignment(eq(1L), any(AssignmentUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAssignment_ShouldReturnNoContent() throws Exception {
        doNothing().when(assignmentService).deleteAssignment(1L);

        mockMvc.perform(delete("/api/v1/assignments/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(assignmentService).deleteAssignment(1L);
    }

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void getAssignmentByTrip_ShouldReturnAssignment() throws Exception {
        when(assignmentService.getAssignmentByTripId(1L)).thenReturn(assignmentResponse);

        mockMvc.perform(get("/api/v1/assignments/trip/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripId").value(1));

        verify(assignmentService).getAssignmentByTripId(1L);
    }

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void getAssignmentsByDriver_ShouldReturnDriverAssignments() throws Exception {
        List<AssignmentResponse> assignments = Arrays.asList(assignmentResponse);
        when(assignmentService.getAssignmentsByDriverId(1L)).thenReturn(assignments);

        mockMvc.perform(get("/api/v1/assignments/driver/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].driverId").value(1));

        verify(assignmentService).getAssignmentsByDriverId(1L);
    }

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void getActiveAssignmentsByDriver_ShouldReturnActiveAssignments() throws Exception {
        List<AssignmentResponse> assignments = Arrays.asList(assignmentResponse);
        when(assignmentService.getActiveAssignmentsByDriver(1L)).thenReturn(assignments);

        mockMvc.perform(get("/api/v1/assignments/driver/1/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].driverId").value(1));

        verify(assignmentService).getActiveAssignmentsByDriver(1L);
    }

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void approveChecklist_ShouldApproveAndReturnAssignment() throws Exception {
        AssignmentResponse approvedResponse = new AssignmentResponse(
                1L, true, LocalDateTime.now(), 1L, "Bogotá - Tunja 08:00",
                "SCHEDULED", LocalDate.now().plusDays(1),
                LocalDateTime.now().plusDays(1).withHour(8), "BOG-TUN",
                1L, "Driver John", 1L, "Dispatcher Mary"
        );
        when(assignmentService.approveChecklist(1L)).thenReturn(approvedResponse);

        mockMvc.perform(post("/api/v1/assignments/1/approve-checklist")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checklistOk").value(true));

        verify(assignmentService).approveChecklist(1L);
    }

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void hasActiveAssignment_ShouldReturnBoolean() throws Exception {
        when(assignmentService.hasActiveAssignment(1L)).thenReturn(true);

        mockMvc.perform(get("/api/v1/assignments/trip/1/has-assignment"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(assignmentService).hasActiveAssignment(1L);
    }

    @Test
    @WithMockUser(roles = "CLERK")
    void createAssignment_WithClerkRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/assignments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void deleteAssignment_WithDispatcherRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/assignments/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}