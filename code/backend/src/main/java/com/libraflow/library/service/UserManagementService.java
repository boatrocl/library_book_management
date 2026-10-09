package com.libraflow.library.service;

import com.libraflow.library.dto.request.UpdateUserRoleRequest;
import com.libraflow.library.dto.request.UpdateUserStatusRequest;
import com.libraflow.library.dto.response.UserManagementResponse;

import java.util.List;

public interface UserManagementService {

    List<UserManagementResponse> getAllUsers();

    UserManagementResponse updateStatus(
            Long userId,
            UpdateUserStatusRequest request,
            String requesterUsername
    );

    UserManagementResponse updateRole(
            Long userId,
            UpdateUserRoleRequest request,
            String requesterUsername
    );
}
