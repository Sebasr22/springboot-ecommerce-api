package com.farmatodo.reto_tecnico.domain.port.out;

import com.farmatodo.reto_tecnico.domain.model.SearchLog;

/**
 * Output port for search log persistence.
 * Defines the contract for storing search query analytics data.
 * Implementation will be provided by the infrastructure layer.
 */
public interface SearchLogRepositoryPort {

    /**
     * Saves a search log entry.
     * @param searchLog the search log to save
     * @return saved search log
     */
    SearchLog save(SearchLog searchLog);
}
