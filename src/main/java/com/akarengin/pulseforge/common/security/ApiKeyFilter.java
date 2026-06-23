package com.akarengin.pulseforge.common.security;

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
    private final ApiKeyValidator apiKeyValidator;
    private static final String API_KEY_HEADER = "X-API-Key";
    private static final Pattern WORKSPACE_PATH_PATTERN = Pattern.compile("/api/workspaces/([^/]+)");

    public ApiKeyFilter(@Lazy ApiKeyValidator apiKeyValidator) {
        this.apiKeyValidator = apiKeyValidator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey == null) {
            filterChain.doFilter(request, response);
            return;
        }
        WorkspaceIdentity workspaceIdentity = apiKeyValidator.validateApiKey(apiKey);
        if (workspaceIdentity == null) {
            handleUnauthorizedRequest(response);
            return;
        }
        UUID pathWorkspaceId = extractWorkspaceIdFromPath(request);
        if (!workspaceIdentity.workspaceId().equals(pathWorkspaceId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "API key does not match workspace");
            return;
        }
        ApiKeyPrincipal principal = new ApiKeyPrincipal(workspaceIdentity);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null,
                Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }

    private void handleUnauthorizedRequest(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API key");
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
