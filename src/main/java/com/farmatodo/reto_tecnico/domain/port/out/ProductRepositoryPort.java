package com.farmatodo.reto_tecnico.domain.port.out;

import com.farmatodo.reto_tecnico.domain.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for product persistence.
 * Defines the contract for product data access operations.
 * Implementation will be provided by the infrastructure layer.
 */
public interface ProductRepositoryPort {

    /**
     * Saves a product (create or update).
     * @param product the product to save
     * @return saved product
     */
    Product save(Product product);

    /**
     * Finds a product by ID.
     * @param id the product ID
     * @return Optional containing the product if found
     */
    Optional<Product> findById(UUID id);

    /**
     * Searches products by name (case-insensitive, partial match).
     * @param query the search query
     * @return list of matching products
     */
    List<Product> findByNameContaining(String query);

    /**
     * Finds all products that are in stock (stock > 0).
     * @return list of products in stock
     */
    List<Product> findAllInStock();

    /**
     * Finds products with stock below a threshold.
     * Useful for low stock alerts.
     * @param threshold the stock threshold
     * @return list of products with low stock
     */
    List<Product> findByStockLessThan(int threshold);

    /**
     * Retrieves all products.
     * @return list of all products
     */
    List<Product> findAll();

    /**
     * Updates stock for a product.
     * @param productId the product ID
     * @param newStock the new stock value
     * @return updated product
     */
    Product updateStock(UUID productId, int newStock);

    /**
     * Deletes a product by ID.
     * @param id the product ID
     * @return true if product was deleted
     */
    boolean deleteById(UUID id);

    /**
     * Checks if a product exists.
     * @param id the product ID
     * @return true if product exists
     */
    boolean existsById(UUID id);
}
