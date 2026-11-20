package com.akarengin.pulseforge.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApiKeyFilter apiKeyFilter;
    private final JwtAuthFilter jwtAuthFilter;
    private final SecurityErrorWriter errorWriter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
        throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/admin/**").permitAll()
                .requestMatchers("/api/users").permitAll()
                .requestMatchers("/api/workspaces").permitAll()
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/api/workspaces/**").authenticated().anyRequest().denyAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    errorWriter.write(response, 401, "Unauthorized")
                )
            )
            // Both filters are mutually exclusive per request (different headers)
            .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
