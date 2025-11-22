package com.bers.services.service;

import com.bers.api.dtos.UserDtos.UserCreateRequest;
import com.bers.api.dtos.UserDtos.UserResponse;
import com.bers.api.dtos.UserDtos.UserSelfUpdateRequest;
import com.bers.api.dtos.UserDtos.UserUpdateRequest;
import com.bers.domain.entities.enums.UserRole;
import com.bers.domain.entities.enums.UserStatus;

import java.util.List;

public interface UserService {
    UserResponse create(UserCreateRequest request);

    UserResponse update(Long id, UserUpdateRequest request);

    UserResponse updateSelf(Long id, UserSelfUpdateRequest request);

    UserResponse getById(Long id);

    UserResponse getByEmail(String email);

    UserResponse getByPhone(String phone);

    List<UserResponse> getAll();

    List<UserResponse> getByRole(UserRole role);

    List<UserResponse> getByRoleAndStatus(UserRole role, UserStatus status);

    void delete(Long id);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    UserResponse changeStatus(Long id, UserStatus status);
}
