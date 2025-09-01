package com.miyazaki.cooperativeproposals.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import jakarta.annotation.PreDestroy;

/**
 * Configuration class to handle graceful shutdown and logging.
 */
@Configuration
@Slf4j
public class ShutdownConfig {

    /**
     * Handles application context closed event.
     * Logs when the Spring context is being closed.
     *
     * @param event the context closed event
     */
    @EventListener
    public void handleContextClosed(final ContextClosedEvent event) {
        log.info("Application context is closing...");
    }

    /**
     * Called before bean destruction during shutdown.
     * Logs the start of the shutdown process.
     */
    @PreDestroy
    public void onShutdown() {
        log.info("Application is shutting down gracefully...");
        try {
            // Give some time for ongoing operations to complete
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Shutdown process was interrupted", e);
        }
        log.info("Shutdown process completed");
    }
}
