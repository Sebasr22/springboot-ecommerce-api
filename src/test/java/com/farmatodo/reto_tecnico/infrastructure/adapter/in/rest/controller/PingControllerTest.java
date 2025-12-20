package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for PingController.
 * Tests the health check endpoint.
 *
 * Note: /ping is whitelisted and doesn't require API Key.
 */
@WebMvcTest(PingController.class)
@DisplayName("PingController REST Tests")
class PingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.farmatodo.reto_tecnico.application.service.AuditLogService auditLogService;

    @Test
    @DisplayName("Should return pong with 200 status and complete response structure")
    void shouldReturnPongWith200Status() throws Exception {
        // When & Then: Call /ping endpoint and verify response
        mockMvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("pong"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.version").exists());
    }
}
