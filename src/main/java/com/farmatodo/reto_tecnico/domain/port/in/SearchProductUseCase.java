package com.farmatodo.reto_tecnico.domain.port.in;

import com.farmatodo.reto_tecnico.domain.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Input port for product search use case.
 * Defines the contract for searching and retrieving product information.
 * Implementation will handle product queries and asynchronous logging.
 */
public interface SearchProductUseCase {

    /**
     * Searches products by name (partial match).
     * Logs the search asynchronously as per business requirements.
     * @param query the search query
     * @return list of matching products
     */
    List<Product> searchByName(String query);

    /**
     * Retrieves a product by its ID.
     * @param productId the product ID
     * @return Optional containing the product if found
     */
    Optional<Product> findById(UUID productId);

    /**
     * Retrieves all products in stock.
     * @return list of products with stock > 0
     */
    List<Product> findAllInStock();

    /**
     * Retrieves all products.
     * @return list of all products
     */
    List<Product> findAll();

    /**
     * Checks if a product has sufficient stock.
     * @param productId the product ID
     * @param quantity the quantity to check
     * @return true if sufficient stock is available
     * @throws com.farmatodo.reto_tecnico.domain.exception.ProductNotFoundException if product not found
     */
    boolean hasStock(UUID productId, int quantity);
}
