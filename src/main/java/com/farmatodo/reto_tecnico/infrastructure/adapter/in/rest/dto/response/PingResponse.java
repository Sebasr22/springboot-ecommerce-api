package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for Ping/Health Check endpoint.
 * Returns basic application status information.
 */
@Data
@Builder
public class PingResponse {

    private String message;

    private LocalDateTime timestamp;

    private String version;

    private String status;
}
