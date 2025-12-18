package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.domain.model.SearchLog;
import com.farmatodo.reto_tecnico.domain.port.out.SearchLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for asynchronous search logging.
 * Separated from ProductServiceImpl to fix AOP self-invocation issue.
 * Spring AOP proxies don't intercept internal method calls within the same bean.
 *
 * Persists search queries to database for analytics and tracking purposes.
 * Follows hexagonal architecture: depends only on domain port, not infrastructure.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchLogService {

    private final SearchLogRepositoryPort searchLogRepository;

    /**
     * Logs search query asynchronously to database.
     * This method runs in a separate thread and does not block the main execution.
     * As per business requirement: "Almacenar las búsquedas realizadas de manera asíncrona"
     *
     * @param query the search query
     * @param resultsCount number of results found
     */
    @Async("taskExecutor")
    public void logSearchAsync(String query, int resultsCount) {
        try {
            // Capture traceId from MDC for correlation logging
            String traceId = MDC.get("traceId");

            log.info("[ASYNC] Recording search to database: query='{}', results={}, traceId={}",
                    query, resultsCount, traceId);

            // Build domain model for search log
            SearchLog searchLog = SearchLog.builder()
                    .id(UUID.randomUUID())
                    .query(query)
                    .resultsCount(resultsCount)
                    .customerId(null) // Customer tracking not implemented yet
                    .searchTimestamp(LocalDateTime.now())
                    .traceId(traceId)
                    .build();

            // Persist via domain port (implementation in infrastructure layer)
            SearchLog saved = searchLogRepository.save(searchLog);

            log.debug("[ASYNC] Search log persisted successfully for query: '{}' with ID: {}",
                    query, saved.getId());
        } catch (Exception e) {
            // Never let async logging failures affect main business flow
            log.error("[ASYNC] Error persisting search log for query: {}", query, e);
        }
    }
}
