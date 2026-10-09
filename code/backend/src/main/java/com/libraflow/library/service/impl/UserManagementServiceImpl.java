package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.User;
import com.libraflow.library.domain.enums.UserRole;
import com.libraflow.library.dto.request.UpdateUserRoleRequest;
import com.libraflow.library.dto.request.UpdateUserStatusRequest;
import com.libraflow.library.dto.response.UserManagementResponse;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import com.libraflow.library.exception.ResourceNotFoundException;
import com.libraflow.library.repository.UserRepository;
import com.libraflow.library.service.UserManagementService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;

    public UserManagementServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserManagementResponse> getAllUsers() {
        return userRepository
                .findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserManagementResponse updateStatus(
            Long userId,
            UpdateUserStatusRequest request,
            String requesterUsername
    ) {
        User user = findUser(userId);
        boolean suspendingAccount = "SUSPENDED".equals(request.status());

        if (suspendingAccount && isRequester(user, requesterUsername)) {
            throw new BusinessException(
                    ErrorCode.ACCESS_DENIED,
                    "Administrators cannot suspend their own account."
            );
        }

        user.setActive("ACTIVE".equals(request.status()));

        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserManagementResponse updateRole(
            Long userId,
            UpdateUserRoleRequest request,
            String requesterUsername
    ) {
        User user = findUser(userId);

        if (isRequester(user, requesterUsername)) {
            throw new BusinessException(
                    ErrorCode.ACCESS_DENIED,
                    "Administrators cannot change their own role."
            );
        }

        user.setRole(
                UserRole.valueOf(request.role())
        );

        return toResponse(userRepository.save(user));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "ไม่พบข้อมูลผู้ใช้รหัส: " + userId
                        )
                );
    }

    private boolean isRequester(User user, String requesterUsername) {
        return user.getUsername().equals(requesterUsername);
    }

    private UserManagementResponse toResponse(User user) {
        return new UserManagementResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getMemberTier(),
                user.isActive()
        );
    }
}
