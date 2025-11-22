package com.bers.services.mappers;

import com.bers.api.dtos.ConfigDtos.ConfigCreateRequest;
import com.bers.api.dtos.ConfigDtos.ConfigResponse;
import com.bers.api.dtos.ConfigDtos.ConfigUpdateRequest;
import com.bers.domain.entities.Config;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ConfigMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "key", source = "key")
    @Mapping(target = "value", source = "value")
    @Mapping(target = "description", source = "description")
    Config toEntity(ConfigCreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "key", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "value", source = "value")
    @Mapping(target = "description", source = "description")
    void updateEntity(ConfigUpdateRequest dto, @MappingTarget Config config);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "key", source = "key")
    @Mapping(target = "value", source = "value")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "updatedAt", source = "updatedAt")
    ConfigResponse toResponse(Config entity);
}