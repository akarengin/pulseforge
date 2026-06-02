package com.akarengin.pulseforge.config;

import com.akarengin.pulseforge.security.ApiKeyFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApiKeyFilter apiKeyFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApiKeyFilter apiKeyFilter)
        throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)  // Disable CSRF for API
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()  // Health checks
                .requestMatchers("/api/workspaces")
                .permitAll()  // Allow workspace creation (POST /api/workspaces)
                .requestMatchers("/api/workspaces/**")
                .authenticated()  // Require auth for workspace operations apikey
                .anyRequest().denyAll()  // Deny everything else
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(401, "Unauthorized"))
            )
            .addFilterBefore(apiKeyFilter,
                UsernamePasswordAuthenticationFilter.class)  // Add your filter
            .httpBasic(AbstractHttpConfigurer::disable)  // CRITICAL: Disable default auth
            .formLogin(AbstractHttpConfigurer::disable);  // Disable form login

        return http.build();
    }

}
