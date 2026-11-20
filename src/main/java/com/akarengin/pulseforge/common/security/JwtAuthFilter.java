package com.akarengin.pulseforge.common.security;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.akarengin.pulseforge.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final SecurityErrorWriter errorWriter;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String header = request.getHeader("Authorization");
        if (isEmpty(header) || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = header.split(" ")[1].trim();
        if (!jwtService.validateToken(token)) {
            errorWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        final UUID userId = jwtService.extractUserId(token);

        userRepository.findById(userId).ifPresentOrElse(
            user -> {
                UserPrincipal principal = new UserPrincipal(user.getId(), user.getEmail());
                var authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            },
            () -> log.warn("JWT references unknown userId: {}", userId)
        );

        filterChain.doFilter(request, response);
    }
}
