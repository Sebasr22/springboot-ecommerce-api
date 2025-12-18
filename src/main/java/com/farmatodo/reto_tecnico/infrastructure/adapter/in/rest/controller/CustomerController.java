package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.controller;

import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.port.in.RegisterCustomerUseCase;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.advice.ErrorResponse;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.request.CreateCustomerRequest;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response.CustomerResponse;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper.CustomerRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for customer management.
 * Provides endpoints for customer registration and operations.
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customers", description = "Customer registration and management API")
public class CustomerController {

    private final RegisterCustomerUseCase registerCustomerUseCase;
    private final CustomerRestMapper mapper;

    @PostMapping
    @Operation(
        summary = "Register new customer",
        description = "Registers a new customer in the system. " +
                      "Email must be unique. " +
                      "Validates all customer data and generates UUID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Customer registered successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomerResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - validation errors",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing API key",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - Customer with email already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<CustomerResponse> registerCustomer(
            @Valid @RequestBody CreateCustomerRequest request
    ) {
        log.info("Registering customer with email: {}", request.getEmail());

        // Convert request to domain
        Customer customer = mapper.toDomain(request);

        // Register customer
        Customer registered = registerCustomerUseCase.register(customer);

        // Convert to response
        CustomerResponse response = mapper.toResponse(registered);
        log.info("Customer registered successfully: {}", registered.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
