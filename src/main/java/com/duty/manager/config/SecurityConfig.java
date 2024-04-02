package com.duty.manager.config;

import com.duty.manager.entity.Role;
import com.duty.manager.service.TimeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Generated;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@EnableWebSecurity
@Generated
@Configuration
public class SecurityConfig {

    public static final String API_V_1_TEMPLATES = "/api/v1/templates/*";
    public static final String ANY_API_V_1 = "/api/v1/**";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final JwtExceptionFilter jwtExceptionFilter;
    private final ObjectMapper objectMapper;
    private final TimeService timeService;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          AuthenticationProvider authenticationProvider,
                          JwtExceptionFilter jwtExceptionFilter,
                          ObjectMapper objectMapper,
                          TimeService timeService,
                          @Qualifier("corsConfigSource")
                          CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationProvider = authenticationProvider;
        this.jwtExceptionFilter = jwtExceptionFilter;
        this.objectMapper = objectMapper;
        this.timeService = timeService;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/jwt").permitAll()
                        .requestMatchers(HttpMethod.GET, "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/participants").hasRole(Role.ADMIN.getName())
                        .requestMatchers(HttpMethod.POST, ANY_API_V_1).hasAnyRole(Role.ADMIN.getName(), Role.PARTICIPANT.getName())
                        .requestMatchers(HttpMethod.PUT, ANY_API_V_1).hasAnyRole(Role.ADMIN.getName(), Role.PARTICIPANT.getName())
                        .requestMatchers(HttpMethod.DELETE, ANY_API_V_1).hasAnyRole(Role.ADMIN.getName(), Role.PARTICIPANT.getName())
                        .requestMatchers(HttpMethod.DELETE, API_V_1_TEMPLATES).hasRole(Role.ADMIN.getName())
                        .requestMatchers(HttpMethod.PUT, API_V_1_TEMPLATES).hasRole(Role.ADMIN.getName())
                        .requestMatchers(HttpMethod.POST, API_V_1_TEMPLATES).hasRole(Role.ADMIN.getName())
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exConf -> exConf.authenticationEntryPoint(
                        new RestResponseAuthenticationEntryPoint(objectMapper, timeService)
                ))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class);
        return http.build();
    }

}
