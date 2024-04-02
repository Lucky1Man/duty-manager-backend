package com.duty.manager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@PropertySource("classpath:application.yml")
public class CorsConfig {

    @Value("${frontend.address}")
    private String frontendAddress;

    @Bean("corsConfigSource")
    @Profile("dev")
    public CorsConfigurationSource devCorsConfigurationSource() {
        return withAllowedOrigins(Arrays.asList("http://localhost", "http://localhost:4200"));
    }

    @Bean("corsConfigSource")
    @Profile("prod")
    public CorsConfigurationSource prodCorsConfigurationSource() {
        return withAllowedOrigins(Arrays.asList(frontendAddress));

    }

    private CorsConfigurationSource withAllowedOrigins(List<String> allowedOrigins) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedOrigins(allowedOrigins);
        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

}
