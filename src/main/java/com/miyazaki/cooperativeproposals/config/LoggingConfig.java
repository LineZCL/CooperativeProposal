package com.miyazaki.cooperativeproposals.config;

import com.miyazaki.cooperativeproposals.filter.RequestTraceFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingConfig {

    /**
     * Register the RequestTraceFilter with order 1.
     * This filter will be applied to all URL patterns.
     *
     * @param filter the RequestTraceFilter to register
     * @return the configured FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<RequestTraceFilter> requestTraceFilterRegistration(final RequestTraceFilter filter) {
        FilterRegistrationBean<RequestTraceFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        registration.setName("requestTraceFilter");
        return registration;
    }
}
