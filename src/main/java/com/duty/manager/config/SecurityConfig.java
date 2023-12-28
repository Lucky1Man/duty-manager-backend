package com.duty.manager.config;

import lombok.Generated;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Generated
@Configuration
public class SecurityConfig {

    public static final String ADMIN = "ADMIN";
    public static final String PARTICIPANT = "PARTICIPANT";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/v1/participants").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/duties/*").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/duties/*").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/v1/duties/*").hasRole(ADMIN)
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

}
