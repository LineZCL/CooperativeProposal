package com.miyazaki.cooperativeproposals.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class RequestTraceFilterTest {

    private final RequestTraceFilter filter = new RequestTraceFilter();

    @Test
    void doFilterInternal_ShouldSetTraceIdFromHeader_WhenHeaderProvided() throws ServletException, IOException {
        final String expectedTraceId = "test-trace-id-123";
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain filterChain = new MockFilterChain();

        request.addHeader(RequestTraceFilter.TRACE_HEADER, expectedTraceId);

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(expectedTraceId, response.getHeader(RequestTraceFilter.TRACE_HEADER));
        assertNull(MDC.get(RequestTraceFilter.TRACE_KEY)); // Should be cleared after filter
    }

    @Test
    void doFilterInternal_ShouldGenerateTraceId_WhenNoHeaderProvided() throws ServletException, IOException {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain filterChain = new MockFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        final String responseTraceId = response.getHeader(RequestTraceFilter.TRACE_HEADER);
        assertNotNull(responseTraceId);
        assertNull(MDC.get(RequestTraceFilter.TRACE_KEY)); // Should be cleared after filter
    }

    @Test
    void doFilterInternal_ShouldGenerateTraceId_WhenHeaderIsBlank() throws ServletException, IOException {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain filterChain = new MockFilterChain();

        request.addHeader(RequestTraceFilter.TRACE_HEADER, "   ");

        filter.doFilterInternal(request, response, filterChain);

        final String responseTraceId = response.getHeader(RequestTraceFilter.TRACE_HEADER);
        assertNotNull(responseTraceId);
        assertNull(MDC.get(RequestTraceFilter.TRACE_KEY)); // Should be cleared after filter
    }
}
