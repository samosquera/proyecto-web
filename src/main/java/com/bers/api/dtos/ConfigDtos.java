package com.bers.api.dtos;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ConfigDtos {
    public record ConfigCreateRequest(
            @NotBlank(message = "key is required")
            String key,
            @NotBlank(message = "value is required")
            String value,
            String description
    ) implements Serializable {
    }

    public record ConfigUpdateRequest(
            @NotBlank(message = "value is required")
            String value,
            String description,
            LocalDateTime updatedAt
    ) implements Serializable {
    }

    public record ConfigResponse(
            Long id,
            String key,
            String value,
            String description,
            LocalDateTime updatedAt
    ) implements Serializable {
    }
}
