package com.miyazaki.cooperativeproposals.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
@Slf4j
public class RequestTraceFilter extends OncePerRequestFilter{

    public static final String TRACE_KEY = "requestTraceId";
    public static final String TRACE_HEADER = "X-Request-Id";
    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        MDC.put(TRACE_KEY, traceId);
        response.setHeader(TRACE_HEADER, traceId);
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_KEY);
        }
    }
}
