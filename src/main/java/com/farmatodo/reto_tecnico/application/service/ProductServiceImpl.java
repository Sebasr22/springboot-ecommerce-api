package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.domain.exception.ProductNotFoundException;
import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.port.in.SearchProductUseCase;
import com.farmatodo.reto_tecnico.domain.port.out.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service implementation for product search operations.
 * Implements asynchronous logging of search queries as per business requirements.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements SearchProductUseCase {

    private final ProductRepositoryPort productRepository;
    private final SearchLogService searchLogService;

    @Override
    public List<Product> searchByName(String query) {
        log.info("Searching products by name: '{}'", query);

        // Validate query
        if (query == null || query.isBlank()) {
            log.warn("Empty search query provided");
            return List.of();
        }

        // Perform search
        List<Product> results = productRepository.findByNameContaining(query.trim());

        // Log search asynchronously (non-blocking) via separate service to fix AOP self-invocation
        searchLogService.logSearchAsync(query, results.size());

        log.info("Found {} products matching query: '{}'", results.size(), query);
        return results;
    }

    @Override
    public Optional<Product> findById(UUID productId) {
        log.debug("Finding product by ID: {}", productId);
        return productRepository.findById(productId);
    }

    @Override
    public List<Product> findAllInStock() {
        log.debug("Finding all products in stock");
        return productRepository.findAllInStock();
    }

    @Override
    public List<Product> findAll() {
        log.debug("Finding all products");
        return productRepository.findAll();
    }

    @Override
    public boolean hasStock(UUID productId, int quantity) {
        log.debug("Checking stock for product {} with quantity {}", productId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return product.hasSufficientStock(quantity);
    }
}
