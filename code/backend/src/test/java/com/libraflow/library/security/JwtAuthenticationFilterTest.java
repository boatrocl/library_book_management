package com.libraflow.library.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();

        filter =
                new JwtAuthenticationFilter(
                        jwtService
                );
    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void filter_shouldContinueWithoutAuthenticationWhenHeaderIsMissing()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain =
                new MockFilterChain();

        filter.doFilter(
                request,
                response,
                chain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verifyNoInteractions(jwtService);
    }

    @Test
    void filter_shouldAuthenticateValidAdminToken()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer valid-token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain =
                new MockFilterChain();

        when(
                jwtService.extractUsername(
                        "valid-token"
                )
        ).thenReturn("admin");

        when(
                jwtService.extractRole(
                        "valid-token"
                )
        ).thenReturn("ADMIN");

        filter.doFilter(
                request,
                response,
                chain
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);

        assertEquals(
                "admin",
                authentication.getName()
        );

        assertTrue(
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority
                                                .getAuthority()
                                                .equals(
                                                        "ROLE_ADMIN"
                                                )
                        )
        );
    }

    @Test
    void filter_shouldClearAuthenticationWhenTokenIsInvalid()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer invalid-token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain =
                new MockFilterChain();

        when(
                jwtService.extractUsername(
                        "invalid-token"
                )
        ).thenThrow(
                new IllegalArgumentException(
                        "Invalid token"
                )
        );

        filter.doFilter(
                request,
                response,
                chain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void filter_shouldCreateMemberAuthority()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer member-token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain =
                new MockFilterChain();

        when(
                jwtService.extractUsername(
                        "member-token"
                )
        ).thenReturn("member01");

        when(
                jwtService.extractRole(
                        "member-token"
                )
        ).thenReturn("MEMBER");

        filter.doFilter(
                request,
                response,
                chain
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);

        assertEquals(
                "member01",
                authentication.getName()
        );

        assertTrue(
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority
                                                .getAuthority()
                                                .equals(
                                                        "ROLE_MEMBER"
                                                )
                        )
        );
    }
}