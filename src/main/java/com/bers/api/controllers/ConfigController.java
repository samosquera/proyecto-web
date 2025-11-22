package com.bers.api.controllers;

import com.bers.api.dtos.ConfigDtos.ConfigCreateRequest;
import com.bers.api.dtos.ConfigDtos.ConfigResponse;
import com.bers.api.dtos.ConfigDtos.ConfigUpdateRequest;
import com.bers.services.service.ConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/configs")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class ConfigController {

    private final ConfigService configService;

    // ==================== CRUD BÁSICO ====================

    @PostMapping("/create")
    public ResponseEntity<ConfigResponse> createConfig(@Valid @RequestBody ConfigCreateRequest request) {
        log.info("Creating config: {}", request.key());

        ConfigResponse created = configService.createConfig(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ConfigResponse>> getAllConfigs() {
        log.debug("Retrieving all configs");

        List<ConfigResponse> configs = configService.getAllConfigs();
        return ResponseEntity.ok(configs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConfigResponse> getConfigById(@PathVariable Long id) {
        log.debug("Retrieving config: {}", id);

        ConfigResponse config = configService.getConfigById(id);
        return ResponseEntity.ok(config);
    }

    @GetMapping("/key/{key}")
    public ResponseEntity<ConfigResponse> getConfigByKey(@PathVariable String key) {
        log.debug("Retrieving config by key: {}", key);

        ConfigResponse config = configService.getConfigByKey(key);
        return ResponseEntity.ok(config);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ConfigResponse> updateConfig(
            @PathVariable Long id,
            @Valid @RequestBody ConfigUpdateRequest request
    ) {
        log.info("Updating config: {}", id);

        ConfigResponse updated = configService.updateConfig(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        log.warn("Deleting config: {}", id);

        configService.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete-key/{key}")
    public ResponseEntity<Void> deleteConfigByKey(@PathVariable String key) {
        log.warn("Deleting config by key: {}", key);

        configService.deleteConfigByKey(key);
        return ResponseEntity.noContent().build();
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    @GetMapping("/key/{key}/value")
    public ResponseEntity<String> getConfigValue(
            @PathVariable String key,
            @RequestParam(required = false) String defaultValue
    ) {
        log.debug("Getting config value for key: {}", key);

        String value = configService.getConfigValue(key, defaultValue);
        return ResponseEntity.ok(value);
    }

    @GetMapping("/key/{key}/value/int")
    public ResponseEntity<Integer> getConfigValueAsInt(
            @PathVariable String key,
            @RequestParam(required = false) Integer defaultValue
    ) {
        log.debug("Getting config value as int for key: {}", key);

        Integer value = configService.getConfigValueAsInt(key, defaultValue);
        return ResponseEntity.ok(value);
    }

    @GetMapping("/key/{key}/value/double")
    public ResponseEntity<Double> getConfigValueAsDouble(
            @PathVariable String key,
            @RequestParam(required = false) Double defaultValue
    ) {
        log.debug("Getting config value as double for key: {}", key);

        Double value = configService.getConfigValueAsDouble(key, defaultValue);
        return ResponseEntity.ok(value);
    }
}