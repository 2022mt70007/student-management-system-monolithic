package com.sms.monolith.security;

import com.sms.common.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars"
    );

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(7);
        if (!JwtUtil.isTokenValid(jwtSecret, token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Claims claims = JwtUtil.parseToken(jwtSecret, token);
        String role = String.valueOf(claims.get("role"));
        if (!isRoleAllowed(path, role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        request = new JwtHeaderRequestWrapper(request, claims);
        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isRoleAllowed(String path, String role) {
        if (path.contains("/internal")) {
            return false;
        }
        if (path.startsWith("/api/admin/")) {
            return "ADMIN".equals(role);
        }
        if (path.startsWith("/api/auth/invitations")) {
            return "ADMIN".equals(role);
        }
        if (path.startsWith("/api/students/")) {
            return Set.of("STUDENT", "ADMIN").contains(role);
        }
        if (path.startsWith("/api/teachers/")) {
            return Set.of("TEACHER", "ADMIN").contains(role);
        }
        if (path.startsWith("/api/academic/")) {
            return "ADMIN".equals(role);
        }
        if (path.startsWith("/api/notifications/")) {
            return Set.of("ADMIN", "TEACHER", "STUDENT").contains(role);
        }
        return true;
    }
}
