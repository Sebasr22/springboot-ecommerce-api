package com.farmatodo.reto_tecnico.domain.port.out;

import com.farmatodo.reto_tecnico.domain.model.AuditLog;

import java.util.List;

/**
 * Output port for audit log persistence.
 * Defines the contract for storing audit logs in the repository.
 * Infrastructure adapters will implement this interface.
 */
public interface AuditLogRepositoryPort {

    /**
     * Saves an audit log entry.
     *
     * @param auditLog the audit log to save
     * @return saved audit log with generated ID
     */
    AuditLog save(AuditLog auditLog);

    /**
     * Finds all audit logs for a specific trace ID.
     * Used to track all events in a single HTTP request/transaction.
     *
     * @param traceId the trace ID to search for
     * @return list of audit logs with matching trace ID
     */
    List<AuditLog> findByTraceId(String traceId);
}
