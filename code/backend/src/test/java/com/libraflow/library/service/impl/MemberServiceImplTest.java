package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.User;
//import com.libraflow.library.domain.entity.UserProfile;
import com.libraflow.library.dto.response.MemberProfileResponse;
import com.libraflow.library.exception.ResourceNotFoundException;
import com.libraflow.library.mapper.MemberMapper;
import com.libraflow.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private MemberServiceImpl memberService;

    private User mockUser;
    private MemberProfileResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockUser = mock(User.class);
        //UserProfile mockProfile = new UserProfile(mockUser, "Pakornkiat", "Srijan", "0800000000", "Khon Kaen");
        
        mockResponse = new MemberProfileResponse(
                1L, "pakornkiat", "test@email.com", "MEMBER", "STUDENT",
                "Pakornkiat", "Srijan", "0800000000", "Khon Kaen"
        );
    }

    @Test
    void getProfile_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(memberMapper.toProfileResponse(mockUser)).thenReturn(mockResponse);

        MemberProfileResponse result = memberService.getProfile(1L);

        assertNotNull(result);
        assertEquals("Pakornkiat", result.firstName());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getProfile_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            memberService.getProfile(99L);
        });
        
        verify(memberMapper, never()).toProfileResponse(any());
    }
}