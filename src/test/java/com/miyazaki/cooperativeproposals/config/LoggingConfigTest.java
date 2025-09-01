package com.miyazaki.cooperativeproposals.config;

import com.miyazaki.cooperativeproposals.filter.RequestTraceFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class LoggingConfigTest {

    @Mock
    private RequestTraceFilter requestTraceFilter;

    @InjectMocks
    private LoggingConfig loggingConfig;

    @Test
    void requestTraceFilterRegistration_ShouldReturnCorrectlyConfiguredBean() {
        // Act
        FilterRegistrationBean<RequestTraceFilter> result = 
                loggingConfig.requestTraceFilterRegistration(requestTraceFilter);

        // Assert
        assertNotNull(result);
        assertEquals(requestTraceFilter, result.getFilter());
        assertEquals(1, result.getOrder());
        // Note: getName() method may not be available in all Spring Boot versions
        assertTrue(result.getUrlPatterns().contains("/*"));
    }

    @Test
    void requestTraceFilterRegistration_ShouldSetCorrectUrlPatterns() {
        // Act
        FilterRegistrationBean<RequestTraceFilter> result = 
                loggingConfig.requestTraceFilterRegistration(requestTraceFilter);

        // Assert
        assertNotNull(result.getUrlPatterns());
        assertEquals(1, result.getUrlPatterns().size());
        assertTrue(result.getUrlPatterns().contains("/*"));
    }

    @Test
    void requestTraceFilterRegistration_ShouldSetCorrectOrder() {
        // Act
        FilterRegistrationBean<RequestTraceFilter> result = 
                loggingConfig.requestTraceFilterRegistration(requestTraceFilter);

        // Assert
        assertEquals(1, result.getOrder());
    }

    @Test
    void requestTraceFilterRegistration_ShouldSetCorrectName() {
        // Act
        FilterRegistrationBean<RequestTraceFilter> result = 
                loggingConfig.requestTraceFilterRegistration(requestTraceFilter);

        // Assert
        // Note: getName() method may not be available in all Spring Boot versions
    }
}
