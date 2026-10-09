package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.User;
import com.libraflow.library.domain.enums.UserRole;
import com.libraflow.library.dto.request.UpdateUserRoleRequest;
import com.libraflow.library.dto.request.UpdateUserStatusRequest;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import com.libraflow.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserManagementServiceImpl userManagementService;

    @BeforeEach
    void setUp() {
        userManagementService = new UserManagementServiceImpl(userRepository);
    }

    @Test
    void updateRole_shouldRejectChangingRequestersOwnRole() {
        User admin = user("admin", UserRole.ADMIN, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userManagementService.updateRole(
                        1L,
                        new UpdateUserRoleRequest("MEMBER"),
                        "admin"
                )
        );

        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateStatus_shouldRejectSuspendingRequestersOwnAccount() {
        User admin = user("admin", UserRole.ADMIN, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userManagementService.updateStatus(
                        1L,
                        new UpdateUserStatusRequest("SUSPENDED"),
                        "admin"
                )
        );

        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateRole_shouldAllowAdminToChangeAnotherUsersRole() {
        User member = user("member01", UserRole.MEMBER, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(member));
        when(userRepository.save(member)).thenReturn(member);

        userManagementService.updateRole(2L, new UpdateUserRoleRequest("LIBRARIAN"), "admin");

        assertEquals(UserRole.LIBRARIAN, member.getRole());
        verify(userRepository).save(member);
    }

    private User user(String username, UserRole role, boolean active) {
        return new User(username, "encoded-password", username + "@example.com", role, active, "STUDENT");
    }
}
