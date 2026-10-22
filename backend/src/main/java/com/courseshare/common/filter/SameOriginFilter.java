package com.courseshare.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component("sameOriginFilter")
public class SameOriginFilter extends OncePerRequestFilter {

    private final Set<String> allowedOrigins;

    public SameOriginFilter(@Value("${security.allowed-origins:}") String configuredOrigins) {
        this.allowedOrigins = Arrays.stream(configuredOrigins.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "GET".equalsIgnoreCase(request.getMethod())
                || "HEAD".equalsIgnoreCase(request.getMethod())
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String origin = request.getHeader("Origin");
        if (origin == null || origin.isBlank() || isAllowed(origin, request)) {
            filterChain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"FORBIDDEN\",\"message\":\"跨站请求被拒绝\",\"data\":null}");
    }

    private boolean isAllowed(String originValue, HttpServletRequest request) {
        try {
            URI origin = URI.create(originValue);
            if (allowedOrigins.contains(origin.getScheme() + "://" + origin.getAuthority())) {
                return true;
            }
            int defaultPort = "https".equalsIgnoreCase(request.getScheme()) ? 443 : 80;
            int actualPort = request.getServerPort();
            int originPort = origin.getPort() > 0 ? origin.getPort() : ("https".equals(origin.getScheme()) ? 443 : 80);
            int targetPort = actualPort > 0 ? actualPort : defaultPort;
            return origin.getScheme().equalsIgnoreCase(request.getScheme())
                    && origin.getHost().equalsIgnoreCase(request.getServerName())
                    && originPort == targetPort;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}

