package com.foodfast.user_service.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * JUnit 5 extension để auto-track test results
 * Được gọi tự động sau mỗi test method
 */
@Component
public class TestMetricsTracker implements TestWatcher {

    private final MeterRegistry meterRegistry;
    private long testStartTime;

    public TestMetricsTracker(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        recordTestResult(context, "passed");
        System.out.println("✅ Test passed: " + context.getDisplayName());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        recordTestResult(context, "failed");
        System.out.println("❌ Test failed: " + context.getDisplayName());
        System.out.println("   Error: " + cause.getMessage());
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        recordTestResult(context, "skipped");
        System.out.println("⏭️ Test skipped: " + context.getDisplayName());
    }

    private void recordTestResult(ExtensionContext context, String status) {
        // Record to Prometheus
        meterRegistry.counter("tests.executed").increment();
        meterRegistry.counter("tests." + status).increment();
        meterRegistry.counter(
            "test.result",
            "status", status,
            "class", context.getTestClass().map(Class::getSimpleName).orElse("Unknown"),
            "method", context.getDisplayName()
        ).increment();
    }
}
