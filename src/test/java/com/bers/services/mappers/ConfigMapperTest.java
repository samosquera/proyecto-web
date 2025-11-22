package com.bers.services.mappers;

import com.bers.api.dtos.ConfigDtos.ConfigCreateRequest;
import com.bers.api.dtos.ConfigDtos.ConfigResponse;
import com.bers.domain.entities.Config;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("ConfigMapper Tests")
class ConfigMapperTest {
    private ConfigMapper configMapper;

    @BeforeEach
    void setUp() {
        configMapper = Mappers.getMapper(ConfigMapper.class);
    }

    @Test
    @DisplayName("Debe mapear ConfigCreateRequest a la entidad Config")
    void shouldMapCreateRequestToEntity() {
        ConfigCreateRequest request = new ConfigCreateRequest(
                "seat.hold.minutes",
                "10",
                "Minutes to hold a seat"
        );

        Config config = configMapper.toEntity(request);

        assertNotNull(config);
        Assertions.assertEquals("seat.hold.minutes", config.getKey());
        Assertions.assertEquals("10", config.getValue());
        Assertions.assertEquals("Minutes to hold a seat", config.getDescription());
        assertNull(config.getId());
    }

    @Test
    @DisplayName("Debe mapear la entidad Config a ConfigResponse")
    void shouldMapEntityToResponse() {
        LocalDateTime updatedAt = LocalDateTime.now();

        Config config = Config.builder()
                .id(1L)
                .key("overbooking.percentage")
                .value("5")
                .description("Max overbooking percentage")
                .updatedAt(updatedAt)
                .build();

        ConfigResponse response = configMapper.toResponse(config);

        assertNotNull(response);
        Assertions.assertEquals(1L, response.id());
        Assertions.assertEquals("overbooking.percentage", response.key());
        Assertions.assertEquals("5", response.value());
        Assertions.assertEquals("Max overbooking percentage", response.description());
        Assertions.assertEquals(updatedAt, response.updatedAt());
    }
}