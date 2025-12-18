package com.farmatodo.reto_tecnico.domain.model;

import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Product domain entity.
 * Represents a product in the catalog with stock management capabilities.
 * Pure domain model without persistence annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    /**
     * Unique identifier for the product.
     */
    private UUID id;

    /**
     * Product name.
     */
    @NotBlank(message = "Product name cannot be blank")
    private String name;

    /**
     * Product description.
     */
    private String description;

    /**
     * Product price (Value Object).
     */
    @NotNull(message = "Price cannot be null")
    private Money price;

    /**
     * Available stock quantity.
     */
    @Min(value = 0, message = "Stock cannot be negative")
    private int stock;

    /**
     * Creates a new product with generated UUID.
     * @param name product name
     * @param description product description
     * @param price product price
     * @param stock initial stock
     * @return new Product instance
     */
    public static Product create(String name, String description, Money price, int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Initial stock cannot be negative");
        }
        return Product.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .build();
    }

    /**
     * Checks if product has sufficient stock for a given quantity.
     * @param quantity the quantity to check
     * @return true if sufficient stock is available
     */
    public boolean hasSufficientStock(int quantity) {
        return this.stock >= quantity;
    }

    /**
     * Checks if product is in stock.
     * @return true if stock is greater than 0
     */
    public boolean isInStock() {
        return this.stock > 0;
    }

    /**
     * Reduces stock by specified quantity.
     * @param quantity the quantity to reduce
     * @throws IllegalArgumentException if quantity is greater than available stock
     */
    public void reduceStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (!hasSufficientStock(quantity)) {
            throw new IllegalArgumentException(
                    String.format("Insufficient stock. Available: %d, Requested: %d", this.stock, quantity)
            );
        }
        this.stock -= quantity;
    }

    /**
     * Increases stock by specified quantity.
     * @param quantity the quantity to add
     */
    public void increaseStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.stock += quantity;
    }

    /**
     * Updates product information.
     * @param name new name
     * @param description new description
     * @param price new price
     */
    public void updateInfo(String name, String description, Money price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    /**
     * Calculates total price for a given quantity.
     * @param quantity the quantity
     * @return total Money amount
     */
    public Money calculateTotal(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        return this.price.multiply(quantity);
    }
}
