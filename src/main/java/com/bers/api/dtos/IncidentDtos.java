package com.bers.api.dtos;

import com.bers.domain.entities.enums.EntityType;
import com.bers.domain.entities.enums.IncidentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

public class IncidentDtos {
    public record IncidentCreateRequest(
            @NotNull(message = "entityType is required")
            EntityType entityType,
            @NotNull(message = "entityId is required")
            Long entityId,
            @NotNull(message = "type is required")
            IncidentType type,
            String note,
            @NotNull(message = "reportedBy is required")
            Long reportedBy
    ) implements Serializable {
    }

    public record IncidentUpdateRequest(
            @NotBlank String note
    ) implements Serializable {
    }

    public record IncidentResponse(
            Long id,
            String entityType,
            Long entityId,
            String type,
            String note,
            Long reportedBy,
            String reportedByName,
            LocalDateTime createdAt
    ) implements Serializable {
    }
}
