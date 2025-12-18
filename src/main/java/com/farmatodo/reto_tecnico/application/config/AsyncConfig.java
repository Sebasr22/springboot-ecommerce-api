package com.farmatodo.reto_tecnico.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Configuration for asynchronous task execution.
 * Defines a bounded ThreadPoolTaskExecutor to prevent unbounded thread creation.
 *
 * CRITICAL FIX: Spring's default SimpleAsyncTaskExecutor creates a new thread for every task
 * with NO LIMIT, which can cause resource exhaustion under load.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Configures a bounded thread pool for async task execution.
     * Used by @Async methods (e.g., SearchLogService.logSearchAsync).
     *
     * Configuration:
     * - Core pool size: 5 threads (always alive)
     * - Max pool size: 10 threads (during peak load)
     * - Queue capacity: 100 tasks (before rejecting)
     * - Rejection policy: CallerRunsPolicy (executes in caller thread if queue full)
     *
     * @return configured task executor
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool size - threads that are always alive
        executor.setCorePoolSize(5);

        // Maximum pool size - threads created during peak load
        executor.setMaxPoolSize(10);

        // Queue capacity - tasks queued when all threads are busy
        executor.setQueueCapacity(100);

        // Thread name prefix for debugging
        executor.setThreadNamePrefix("async-exec-");

        // Rejection policy when queue is full
        // CallerRunsPolicy: Execute in the caller's thread (provides back-pressure)
        // Alternatives: AbortPolicy (throw exception), DiscardPolicy (silent drop)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Wait for tasks to complete on shutdown (max 30 seconds)
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }
}
