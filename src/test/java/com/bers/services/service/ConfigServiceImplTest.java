package com.bers.services.service;

import com.bers.api.dtos.ConfigDtos.ConfigCreateRequest;
import com.bers.api.dtos.ConfigDtos.ConfigResponse;
import com.bers.domain.entities.Config;
import com.bers.domain.repositories.ConfigRepository;
import com.bers.services.mappers.ConfigMapper;
import com.bers.services.service.serviceImple.ConfigServiceImpl;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfigService test")
class ConfigServiceImplTest {
    @Mock
    private ConfigRepository configRepository;
    @Spy
    private ConfigMapper configMapper = Mappers.getMapper(ConfigMapper.class);
    @InjectMocks
    private ConfigServiceImpl configService;
    private Config config;
    private ConfigCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        config = Config.builder()
                .id(1L)
                .key("seat.hold.minutes")
                .value("10")
                .description("Minutes to hold seat")
                .updatedAt(LocalDateTime.now())
                .build();
        createRequest = new ConfigCreateRequest(
                "seat.hold.minutes", "10", "Minutes to hold seat"
        );
    }

    @Test
    @DisplayName("Debe crear config exitosamente")
    void shouldCreateConfigSuccessfully() {
        when(configRepository.existsByKey(any())).thenReturn(false);
        when(configRepository.save(any())).thenReturn(config);

        ConfigResponse result = configService.createConfig(createRequest);

        assertNotNull(result);
        Assertions.assertEquals("seat.hold.minutes", result.key());
        verify(configRepository).save(any(Config.class));
        verify(configMapper).toEntity(createRequest);
        verify(configMapper).toResponse(any(Config.class));
    }

    @Test
    @DisplayName("Debe lanzar exception cuando la key ya existe")
    void shouldThrowExceptionWhenKeyAlreadyExists() {
        when(configRepository.existsByKey(any())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> configService.createConfig(createRequest)
        );

        Assertions.assertTrue(exception.getMessage().contains("already exists"));
        verify(configRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe obtener config value con valor por default")
    void shouldGetConfigValueWithDefault() {
        when(configRepository.findByKey("missing.key")).thenReturn(Optional.empty());

        String result = configService.getConfigValue("missing.key", "default");

        Assertions.assertEquals("default", result);
    }

    @Test
    @DisplayName("Debe obtener config value como int")
    void shouldGetConfigValueAsInt() {
        when(configRepository.findByKey("seat.hold.minutes"))
                .thenReturn(Optional.of(config));

        Integer result = configService.getConfigValueAsInt("seat.hold.minutes", 0);

        Assertions.assertEquals(10, result);
    }

    @Test
    @DisplayName("Debe retornar default value con invalid format")
    void shouldReturnDefaultWithInvalidFormat() {
        config.setValue("invalid");
        when(configRepository.findByKey("seat.hold.minutes"))
                .thenReturn(Optional.of(config));

        Integer result = configService.getConfigValueAsInt("seat.hold.minutes", 5);

        Assertions.assertEquals(5, result);
    }

    @Test
    @DisplayName("Debe obtener value como double")
    void shouldGetValueAsDouble() {
        config.setValue("5.5");
        when(configRepository.findByKey("test.double")).thenReturn(Optional.of(config));

        Double result = configService.getConfigValueAsDouble("test.double", 0.0);

        Assertions.assertEquals(5.5, result);
    }

    @Test
    @DisplayName("Debe eliminar config por key")
    void shouldDeleteConfigByKey() {
        when(configRepository.existsByKey("test.key")).thenReturn(true);

        configService.deleteConfigByKey("test.key");

        verify(configRepository).deleteByKey("test.key");
    }
}