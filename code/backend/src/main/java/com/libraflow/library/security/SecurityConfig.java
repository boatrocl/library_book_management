package com.libraflow.library.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final String allowedOrigin;

    public SecurityConfig(
            @Value("${app.cors.allowed-origin}")
            String allowedOrigin
    ) {
        this.allowedOrigin = allowedOrigin;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {

        http

                .csrf(
                        csrf ->
                                csrf.disable()
                )

                .cors(
                        cors ->
                                cors.configurationSource(
                                        corsConfigurationSource()
                                )
                )

                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )

                .exceptionHandling(
                        exceptions ->
                                exceptions

                                        .authenticationEntryPoint(
                                                (
                                                        request,
                                                        response,
                                                        authException
                                                ) ->
                                                        response.sendError(
                                                                HttpStatus.UNAUTHORIZED.value(),
                                                                "Unauthorized"
                                                        )
                                        )

                                        .accessDeniedHandler(
                                                (
                                                        request,
                                                        response,
                                                        accessDeniedException
                                                ) ->
                                                        response.sendError(
                                                                HttpStatus.FORBIDDEN.value(),
                                                                "Forbidden"
                                                        )
                                        )
                )

                .authorizeHttpRequests(
                        authorize ->
                                authorize

                                        .requestMatchers(
                                                HttpMethod.OPTIONS,
                                                "/**"
                                        )
                                        .permitAll()

                                        .requestMatchers(
                                                "/api/v1/auth/**",
                                                "/swagger-ui.html",
                                                "/swagger-ui/**",
                                                "/v3/api-docs/**",
                                                "/error"
                                        )
                                        .permitAll()

                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/v1/categories",
                                                "/api/v1/categories/**",
                                                "/api/v1/books/**"
                                        )
                                        .permitAll()

                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/api/v1/books/**"
                                        )
                                        .hasAnyRole(
                                                "ADMIN",
                                                "LIBRARIAN"
                                        )

                                        .requestMatchers(
                                                HttpMethod.PUT,
                                                "/api/v1/books/**"
                                        )
                                        .hasAnyRole(
                                                "ADMIN",
                                                "LIBRARIAN"
                                        )

                                        .requestMatchers(
                                                HttpMethod.PATCH,
                                                "/api/v1/books/**"
                                        )
                                        .hasAnyRole(
                                                "ADMIN",
                                                "LIBRARIAN"
                                        )

                                        .requestMatchers(
                                                HttpMethod.DELETE,
                                                "/api/v1/books/**"
                                        )
                                        .hasAnyRole(
                                                "ADMIN",
                                                "LIBRARIAN"
                                        )

                                        .anyRequest()
                                        .authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource
    corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        allowedOrigin
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept"
                )
        );

        configuration.setExposedHeaders(
                List.of(
                        "Location"
                )
        );

        configuration.setAllowCredentials(
                true
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}
