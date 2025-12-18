package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.controller;

import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.port.in.SearchProductUseCase;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.advice.ErrorResponse;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response.ProductResponse;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper.ProductRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for product search operations.
 * Provides endpoints for searching and listing products.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Product catalog and search API")
public class ProductController {

    private final SearchProductUseCase searchProductUseCase;
    private final ProductRestMapper mapper;

    @GetMapping
    @Operation(
        summary = "Search products",
        description = "Search products by name (case-insensitive partial match). " +
                      "If no query provided, returns all in-stock products. " +
                      "Supports simple pagination with page and size parameters."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Products found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductResponse.class)
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
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @Parameter(description = "Search query (product name)", example = "Acetaminofén")
            @RequestParam(required = false) String search,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        log.info("Searching products with query: '{}', page: {}, size: {}", search, page, size);

        List<Product> products;
        if (search == null || search.isBlank()) {
            log.debug("No search query provided, returning all in-stock products");
            products = searchProductUseCase.findAllInStock();
        } else {
            products = searchProductUseCase.searchByName(search);
        }

        // Simple pagination
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, products.size());

        List<Product> paginatedProducts = products.subList(
            Math.min(startIndex, products.size()),
            endIndex
        );

        List<ProductResponse> response = mapper.toResponseList(paginatedProducts);
        log.info("Found {} products (page {}, showing {})", products.size(), page, response.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @Operation(
        summary = "List all products",
        description = "Retrieves all products in the catalog, including out-of-stock items."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Products retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductResponse.class)
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
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("Retrieving all products");

        List<Product> products = searchProductUseCase.findAll();
        List<ProductResponse> response = mapper.toResponseList(products);

        log.info("Retrieved {} products", response.size());
        return ResponseEntity.ok(response);
    }
}
