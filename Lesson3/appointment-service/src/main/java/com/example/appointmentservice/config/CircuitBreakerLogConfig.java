package com.example.appointmentservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CircuitBreakerLogConfig {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerLogConfig.class);
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerLogConfig(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @PostConstruct
    public void registerEventListeners() {
        circuitBreakerRegistry.circuitBreaker("doctorServiceCB")
                .getEventPublisher()
                .onStateTransition(event ->
                        log.warn("========================================================\n" +
                                 "[CIRCUIT BREAKER STATE CHANGED] doctorServiceCB: {} -> {}\n" +
                                 "========================================================",
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState())
                )
                .onError(event ->
                        log.error("[CIRCUIT BREAKER ERROR] Call failed: {}", event.getThrowable().getMessage())
                )
                .onSuccess(event ->
                        log.info("[CIRCUIT BREAKER SUCCESS] Call succeeded, duration: {} ms", event.getElapsedDuration().toMillis())
                );
    }
}
