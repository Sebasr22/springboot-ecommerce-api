package com.farmatodo.reto_tecnico.infrastructure.config;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * TaskDecorator that propagates MDC (Mapped Diagnostic Context) to async threads.
 *
 * CRITICAL: Without this decorator, trace IDs are LOST when using @Async
 * because MDC uses ThreadLocal which doesn't propagate across threads.
 *
 * How it works:
 * 1. Captures MDC from calling thread (request thread) BEFORE task execution
 * 2. Restores MDC in async thread BEFORE task runs
 * 3. Clears MDC after task completes to prevent thread pollution
 *
 * This ensures that:
 * - AsyncEmailService has access to trace ID when sending emails
 * - SearchLogService has access to trace ID when logging searches
 * - AuditLogService has access to trace ID when logging events
 *
 * Usage:
 * Configure in AsyncConfig:
 * {@code
 * ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
 * executor.setTaskDecorator(new MdcTaskDecorator());
 * }
 *
 * Example:
 * {@code
 * // Main thread (HTTP request)
 * MDC.put("traceId", "abc-123-def-456");
 *
 * // Async method
 * @Async("taskExecutor")
 * public void doSomethingAsync() {
 *     String traceId = MDC.get("traceId"); // Now returns "abc-123-def-456" ✅
 * }
 * }
 */
public class MdcTaskDecorator implements TaskDecorator {

    /**
     * Decorates the given Runnable to propagate MDC context.
     *
     * @param runnable the task to execute
     * @return wrapped runnable with MDC context propagation
     */
    @Override
    public Runnable decorate(Runnable runnable) {
        // STEP 1: Capture MDC from calling thread (request thread)
        // This happens BEFORE the task is submitted to the thread pool
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        // STEP 2: Return wrapped runnable that will execute in async thread
        return () -> {
            try {
                // STEP 3: Restore MDC in async thread BEFORE running the task
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }

                // STEP 4: Execute the actual task
                // Now MDC.get("traceId") will work inside @Async methods
                runnable.run();

            } finally {
                // STEP 5: Clear MDC after task completes
                // CRITICAL: Prevents MDC from leaking to other tasks
                // when threads are reused from the pool
                MDC.clear();
            }
        };
    }
}
