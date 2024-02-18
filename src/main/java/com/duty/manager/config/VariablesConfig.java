package com.duty.manager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VariablesConfig {

    @Value("${application.security.jwt.secret-key}")
    private String jwtKey;

    @Value("${application.security.jwt.expiration}")
    private Long expirationTime;

    @Bean
    public String jwtKey() {
        return jwtKey;
    }

    @Bean
    public Long expirationTime() {
        return expirationTime;
    }

}
