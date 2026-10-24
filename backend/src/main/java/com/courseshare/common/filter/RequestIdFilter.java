package com.courseshare.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component("requestIdFilter")
public class RequestIdFilter extends OncePerRequestFilter {

    private static final Pattern VALID_ID = Pattern.compile("[A-Za-z0-9_-]{8,64}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String provided = request.getHeader("X-Request-Id");
        String requestId = provided != null && VALID_ID.matcher(provided).matches()
                ? provided
                : UUID.randomUUID().toString().replace("-", "");
        request.setAttribute("requestId", requestId);
        response.setHeader("X-Request-Id", requestId);
        MDC.put("requestId", requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("requestId");
        }
    }
}

