package com.foodfast.user_service.base;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base test class với metrics tracking
 * Extend class này để auto-track test results
 */
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith({TestMetricsExtension.class})
public abstract class BaseTest {

    @Autowired(required = false)
    protected MeterRegistry meterRegistry;

    /**
     * Helper method để manually record metrics
     */
    protected void recordMetric(String metricName, double value) {
        if (meterRegistry != null) {
            Counter.builder(metricName)
                    .register(meterRegistry)
                    .increment(value);
        }
    }

    /**
     * Helper method để record test result
     */
    protected void recordTestResult(String testName, boolean passed) {
        if (meterRegistry != null) {
            String status = passed ? "passed" : "failed";
            meterRegistry.counter("tests." + status).increment();
            meterRegistry.counter(
                "test.result",
                "status", status,
                "name", testName
            ).increment();
        }
    }
}
