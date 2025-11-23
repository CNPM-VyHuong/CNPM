package com.foodfast.user_service.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration để enable metrics tracking cho unit tests
 */
@TestConfiguration
public class TestMetricsConfig {

    @Bean
    @Primary
    public MeterRegistry testMeterRegistry() {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(
            io.micrometer.prometheus.PrometheusConfig.DEFAULT
        );
        
        // Initialize test counters
        Counter.builder("tests.executed")
                .description("Total number of tests executed")
                .register(registry);
        
        Counter.builder("tests.passed")
                .description("Total number of passed tests")
                .register(registry);
        
        Counter.builder("tests.failed")
                .description("Total number of failed tests")
                .register(registry);
        
        return registry;
    }

    @Bean
    public TestMetricsTracker testMetricsTracker(MeterRegistry meterRegistry) {
        return new TestMetricsTracker(meterRegistry);
    }
}
