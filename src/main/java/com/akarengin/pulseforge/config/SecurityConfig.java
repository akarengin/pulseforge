package com.akarengin.pulseforge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // Disable security to test basic endpoint
    // TODO: Day 3 - Add JWT and API key authentication
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .csrf(CsrfConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
