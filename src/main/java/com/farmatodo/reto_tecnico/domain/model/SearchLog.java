package com.farmatodo.reto_tecnico.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model for search log entries.
 * Represents a search query performed by a user for analytics and tracking purposes.
 */
@Value
@Builder
public class SearchLog {
    UUID id;
    String query;
    Integer resultsCount;
    UUID customerId;
    LocalDateTime searchTimestamp;
    String traceId;
}
