package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.controller;

import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response.PingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Health Check Controller.
 * Provides a simple ping endpoint to verify the application is running.
 */
@RestController
@RequestMapping
@Tag(name = "Health Check", description = "Endpoints for application health verification")
public class PingController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${project.version:0.0.1-SNAPSHOT}")
    private String version;

    @Operation(
            summary = "Ping endpoint",
            description = "Returns 'pong' to verify the application is running and responding to requests"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "System is healthy and responding",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PingResponse.class)
            )
        )
    })
    @GetMapping("/ping")
    public ResponseEntity<PingResponse> ping() {
        PingResponse response = PingResponse.builder()
                .message("pong")
                .timestamp(LocalDateTime.now())
                .version(version)
                .status("UP")
                .build();

        return ResponseEntity.ok(response);
    }
}
