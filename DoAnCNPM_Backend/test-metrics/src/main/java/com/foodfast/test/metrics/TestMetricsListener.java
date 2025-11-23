package com.foodfast.test.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Global TestExecutionListener để track test execution metrics
 * Được dùng cho tất cả services
 */
public class TestMetricsListener implements TestExecutionListener {

    private final MeterRegistry meterRegistry;
    private final AtomicLong testStartTime = new AtomicLong();

    private Counter testPassedCounter;
    private Counter testFailedCounter;
    private Counter testSkippedCounter;
    private Timer testDurationTimer;

    public TestMetricsListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initializeMetrics();
    }

    private void initializeMetrics() {
        testPassedCounter = Counter.builder("tests.passed")
                .description("Total number of passed tests")
                .register(meterRegistry);

        testFailedCounter = Counter.builder("tests.failed")
                .description("Total number of failed tests")
                .register(meterRegistry);

        testSkippedCounter = Counter.builder("tests.skipped")
                .description("Total number of skipped tests")
                .register(meterRegistry);

        testDurationTimer = Timer.builder("tests.duration")
                .description("Test execution duration")
                .register(meterRegistry);
    }

    @Override
    public void executionStarted(org.junit.platform.engine.TestDescriptor testDescriptor) {
        testStartTime.set(System.currentTimeMillis());
    }

    @Override
    public void executionFinished(org.junit.platform.engine.TestDescriptor testDescriptor,
                                   TestExecutionResult testExecutionResult) {
        long duration = System.currentTimeMillis() - testStartTime.get();
        String testName = testDescriptor.getDisplayName();

        // Record duration
        testDurationTimer.record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);

        // Track result
        switch (testExecutionResult.getStatus()) {
            case SUCCESSFUL:
                testPassedCounter.increment();
                meterRegistry.counter("test.result", "status", "passed", "name", testName).increment();
                System.out.println("✅ PASSED: " + testName + " (" + duration + "ms)");
                break;

            case FAILED:
                testFailedCounter.increment();
                meterRegistry.counter("test.result", "status", "failed", "name", testName).increment();
                System.out.println("❌ FAILED: " + testName);
                if (testExecutionResult.getThrowable().isPresent()) {
                    Throwable throwable = testExecutionResult.getThrowable().get();
                    System.out.println("   Error: " + throwable.getMessage());
                    meterRegistry.counter("test.error", "class", throwable.getClass().getSimpleName()).increment();
                }
                break;

            case ABORTED:
                testSkippedCounter.increment();
                meterRegistry.counter("test.result", "status", "skipped", "name", testName).increment();
                System.out.println("⏭️ SKIPPED: " + testName);
                break;
        }
    }

    // Getters for metrics
    public double getPassedCount() {
        return testPassedCounter.count();
    }

    public double getFailedCount() {
        return testFailedCounter.count();
    }

    public double getSkippedCount() {
        return testSkippedCounter.count();
    }

    public double getTotalTests() {
        return getPassedCount() + getFailedCount() + getSkippedCount();
    }

    public double getSuccessRate() {
        double total = getTotalTests();
        return total > 0 ? (getPassedCount() / total) * 100 : 0;
    }
}
