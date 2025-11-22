package com.bers.services.service;

import com.bers.api.dtos.ConfigDtos.ConfigCreateRequest;
import com.bers.api.dtos.ConfigDtos.ConfigResponse;
import com.bers.api.dtos.ConfigDtos.ConfigUpdateRequest;

import java.util.List;

public interface ConfigService {
    ConfigResponse createConfig(ConfigCreateRequest request);

    ConfigResponse updateConfig(Long id, ConfigUpdateRequest request);

    ConfigResponse getConfigById(Long id);

    ConfigResponse getConfigByKey(String key);

    List<ConfigResponse> getAllConfigs();

    void deleteConfig(Long id);

    void deleteConfigByKey(String key);

    String getConfigValue(String key, String defaultValue);

    Integer getConfigValueAsInt(String key, Integer defaultValue);

    Double getConfigValueAsDouble(String key, Double defaultValue);
}
