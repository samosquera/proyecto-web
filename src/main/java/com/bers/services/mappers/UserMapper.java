package com.bers.services.mappers;

import com.bers.api.dtos.UserDtos.UserCreateRequest;
import com.bers.api.dtos.UserDtos.UserResponse;
import com.bers.api.dtos.UserDtos.UserUpdateRequest;
import com.bers.domain.entities.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    User toEntity(UserCreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "dateOfBirth", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "username", source = "username")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "status", source = "status")
    void updateEntity(UserUpdateRequest dto, @MappingTarget User user);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "createAt", source = "createAt")
    UserResponse toResponse(User entity);
}