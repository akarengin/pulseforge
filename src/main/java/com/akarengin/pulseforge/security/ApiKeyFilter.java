package com.akarengin.pulseforge.security;

import com.akarengin.pulseforge.entity.Workspace;
import com.akarengin.pulseforge.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {
    private final ApiKeyService apiKeyService;
    private static final String API_KEY_HEADER = "X-API-Key";
    private static final Pattern WORKSPACE_PATH_PATTERN =
        Pattern.compile("/api/workspaces/([^/]+)");

    // Startup phase
    public ApiKeyFilter(@Lazy ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    // ↑ apiKeyService is a proxy, real bean not created yet
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey == null) {
            filterChain.doFilter(request, response);
            return;
        }
        // First HTTP request arrives
        Workspace workspace = apiKeyService.validateApiKey(apiKey);
        // ↑ NOW the real ApiKeyService bean is created (DI @Lazy triggered)
        if (workspace == null) {
            handleUnauthorizedRequest(response);
            return;
        }
        UUID pathWorkspaceId = extractWorkspaceIdFromPath(request);
        if (!workspace.getId().equals(pathWorkspaceId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        ApiKeyPrincipal principal = new ApiKeyPrincipal(workspace);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }

    private void handleUnauthorizedRequest(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private UUID extractWorkspaceIdFromPath(HttpServletRequest request) {
        Matcher matcher = WORKSPACE_PATH_PATTERN.matcher(request.getRequestURI());
        if (matcher.find()) {
            try {
                return UUID.fromString(matcher.group(1));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}
