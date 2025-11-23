package com.foodfast.user_service.base;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.springframework.context.ApplicationContext;
import java.util.Optional;

/**
 * JUnit 5 Extension để auto-track test execution results
 */
public class TestMetricsExtension implements TestWatcher {

    private MeterRegistry getMeterRegistry(ExtensionContext context) {
        Optional<ApplicationContext> appContext = context.getStore(ExtensionContext.Namespace.GLOBAL)
                .get("appContext", ApplicationContext.class);
        
        if (appContext.isPresent()) {
            try {
                return appContext.get().getBean(MeterRegistry.class);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        MeterRegistry registry = getMeterRegistry(context);
        if (registry != null) {
            registry.counter("tests.executed").increment();
            registry.counter("tests.passed").increment();
            registry.counter(
                "test.result",
                "status", "passed",
                "test", context.getDisplayName()
            ).increment();
        }
        System.out.println("✅ PASSED: " + context.getDisplayName());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        MeterRegistry registry = getMeterRegistry(context);
        if (registry != null) {
            registry.counter("tests.executed").increment();
            registry.counter("tests.failed").increment();
            registry.counter(
                "test.result",
                "status", "failed",
                "test", context.getDisplayName()
            ).increment();
        }
        System.out.println("❌ FAILED: " + context.getDisplayName());
        System.out.println("   Error: " + cause.getMessage());
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        System.out.println("⏭️ SKIPPED: " + context.getDisplayName());
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        System.out.println("⏸️ DISABLED: " + context.getDisplayName());
    }
}
