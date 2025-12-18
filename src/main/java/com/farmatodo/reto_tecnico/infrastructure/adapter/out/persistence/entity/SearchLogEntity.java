package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for search logs.
 * Stores search queries for analytics and tracking purposes.
 */
@Entity
@Table(name = "search_logs", indexes = {
        @Index(name = "idx_search_log_timestamp", columnList = "search_timestamp"),
        @Index(name = "idx_search_log_trace_id", columnList = "trace_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchLogEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "query", length = 500)
    private String query;

    @Column(name = "results_count", nullable = false)
    private Integer resultsCount;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "search_timestamp", nullable = false)
    private LocalDateTime searchTimestamp;

    @Column(name = "trace_id", length = 36)
    private String traceId;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (searchTimestamp == null) {
            searchTimestamp = LocalDateTime.now();
        }
    }
}
