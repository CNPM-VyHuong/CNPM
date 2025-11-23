package com.foodfast.test.listener;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.springframework.stereotype.Component;

/**
 * TestExecutionListener để track test results và gửi metrics tới Prometheus
 */
@Component
public class TestMetricsListener implements TestExecutionListener {

    private final MeterRegistry meterRegistry;
    private Counter testPassedCounter;
    private Counter testFailedCounter;
    private Counter testSkippedCounter;

    public TestMetricsListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initializeMetrics();
    }

    private void initializeMetrics() {
        this.testPassedCounter = Counter.builder("test.passed")
                .description("Số lượng test passed")
                .register(meterRegistry);

        this.testFailedCounter = Counter.builder("test.failed")
                .description("Số lượng test failed")
                .register(meterRegistry);

        this.testSkippedCounter = Counter.builder("test.skipped")
                .description("Số lượng test skipped")
                .register(meterRegistry);
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        System.out.println("🚀 Test Plan Started: " + testPlan.getChildren().size() + " tests");
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        System.out.println("✅ Test Plan Finished");
        System.out.println("   Passed: " + testPassedCounter.count());
        System.out.println("   Failed: " + testFailedCounter.count());
        System.out.println("   Skipped: " + testSkippedCounter.count());
    }

    @Override
    public void executionFinished(org.junit.platform.engine.TestDescriptor testDescriptor,
                                   TestExecutionResult testExecutionResult) {
        if (testDescriptor.getSource().isPresent()) {
            TestSource source = testDescriptor.getSource().get();
            String testName = testDescriptor.getDisplayName();

            switch (testExecutionResult.getStatus()) {
                case SUCCESSFUL:
                    testPassedCounter.increment();
                    System.out.println("✅ PASSED: " + testName);
                    meterRegistry.counter("test.passed.total").increment();
                    break;

                case FAILED:
                    testFailedCounter.increment();
                    System.out.println("❌ FAILED: " + testName);
                    if (testExecutionResult.getThrowable().isPresent()) {
                        System.out.println("   Error: " + testExecutionResult.getThrowable().get().getMessage());
                    }
                    meterRegistry.counter("test.failed.total").increment();
                    break;

                case ABORTED:
                    testSkippedCounter.increment();
                    System.out.println("⏭️ SKIPPED: " + testName);
                    meterRegistry.counter("test.skipped.total").increment();
                    break;
            }
        }
    }
}
