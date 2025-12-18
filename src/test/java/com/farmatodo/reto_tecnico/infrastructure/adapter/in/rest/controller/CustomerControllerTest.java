package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.controller;

import com.farmatodo.reto_tecnico.domain.exception.CustomerAlreadyExistsException;
import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;
import com.farmatodo.reto_tecnico.domain.port.in.RegisterCustomerUseCase;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper.CustomerRestMapperImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for CustomerController.
 * Tests HTTP endpoints and global exception handling.
 *
 * Uses @WebMvcTest to load only web layer (MockMvc).
 * Uses @Import to include MapStruct mapper implementations for coverage.
 */
@WebMvcTest(CustomerController.class)
@Import(CustomerRestMapperImpl.class)
@DisplayName("CustomerController REST Tests")
class CustomerControllerTest {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_KEY_VALUE = "default-dev-key-change-in-production";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterCustomerUseCase registerCustomerUseCase;

    @Test
    @DisplayName("Should register customer successfully with 201 status")
    void shouldRegisterCustomer() throws Exception {
        // Given: Valid request
        String requestBody = """
            {
                "name": "Juan Pérez García",
                "email": "juan.perez@example.com",
                "phone": "3001234567",
                "address": "Calle 123 #45-67, Bogotá"
            }
            """;

        // Mock use case response
        Customer registeredCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .name("Juan Pérez García")
                .email(new Email("juan.perez@example.com"))
                .phone(new Phone("3001234567"))
                .address("Calle 123 #45-67, Bogotá")
                .build();

        when(registerCustomerUseCase.register(any(Customer.class))).thenReturn(registeredCustomer);

        // When & Then: Call endpoint and verify response
        mockMvc.perform(post("/api/v1/customers")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Juan Pérez García"))
                .andExpect(jsonPath("$.email").value("juan.perez@example.com"))
                .andExpect(jsonPath("$.phone").value("3001234567"))
                .andExpect(jsonPath("$.address").value("Calle 123 #45-67, Bogotá"));

        // Verify use case was called
        verify(registerCustomerUseCase, times(1)).register(any(Customer.class));
    }

    @Test
    @DisplayName("Should return 409 Conflict when email already exists")
    void shouldReturnConflictWhenEmailExists() throws Exception {
        // Given: Request with existing email
        String requestBody = """
            {
                "name": "Juan Pérez",
                "email": "existing@example.com",
                "phone": "3001234567",
                "address": "Calle 123, Bogotá"
            }
            """;

        // Mock use case throws exception
        when(registerCustomerUseCase.register(any(Customer.class)))
                .thenThrow(new CustomerAlreadyExistsException("existing@example.com"));

        // When & Then: Verify 409 status and error response
        mockMvc.perform(post("/api/v1/customers")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CUSTOMER_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value(containsString("existing@example.com")));

        verify(registerCustomerUseCase, times(1)).register(any(Customer.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request on invalid email")
    void shouldReturnBadRequestOnInvalidInput() throws Exception {
        // Given: Request with invalid email
        String requestBody = """
            {
                "name": "Juan Pérez",
                "email": "invalid-email",
                "phone": "3001234567",
                "address": "Calle 123, Bogotá"
            }
            """;

        // When & Then: Verify 400 status with validation errors
        mockMvc.perform(post("/api/v1/customers")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.email").value(containsString("must be valid")));

        // Use case should NOT be called due to validation failure
        verify(registerCustomerUseCase, never()).register(any(Customer.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when name is too short")
    void shouldReturnBadRequestWhenNameTooShort() throws Exception {
        // Given: Request with name < 2 characters
        String requestBody = """
            {
                "name": "A",
                "email": "test@example.com",
                "phone": "3001234567",
                "address": "Calle 123, Bogotá"
            }
            """;

        // When & Then: Verify validation error
        mockMvc.perform(post("/api/v1/customers")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.name").exists());

        verify(registerCustomerUseCase, never()).register(any(Customer.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when phone is invalid")
    void shouldReturnBadRequestWhenPhoneInvalid() throws Exception {
        // Given: Request with invalid phone (contains letters)
        String requestBody = """
            {
                "name": "Juan Pérez",
                "email": "test@example.com",
                "phone": "ABC123",
                "address": "Calle 123, Bogotá"
            }
            """;

        // When & Then: Verify validation error
        mockMvc.perform(post("/api/v1/customers")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.phone").value(containsString("digits")));

        verify(registerCustomerUseCase, never()).register(any(Customer.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when address is blank")
    void shouldReturnBadRequestWhenAddressBlank() throws Exception {
        // Given: Request with blank address
        String requestBody = """
            {
                "name": "Juan Pérez",
                "email": "test@example.com",
                "phone": "3001234567",
                "address": ""
            }
            """;

        // When & Then: Verify validation error
        mockMvc.perform(post("/api/v1/customers")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.address").exists());

        verify(registerCustomerUseCase, never()).register(any(Customer.class));
    }
}
