package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.domain.exception.ProductNotFoundException;
import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import com.farmatodo.reto_tecnico.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductServiceImpl.
 * Tests product search and async logging functionality.
 *
 * Uses pure unit testing with Mockito (NO Spring context).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Unit Tests")
class ProductServiceImplTest {

    @Mock
    private ProductRepositoryPort productRepository;

    @Mock
    private SearchLogService searchLogService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        testProduct1 = Product.builder()
                .id(UUID.randomUUID())
                .name("Acetaminofén 500mg")
                .description("Analgésico y antipirético")
                .price(new Money(new BigDecimal("10000.00")))
                .stock(100)
                .build();

        testProduct2 = Product.builder()
                .id(UUID.randomUUID())
                .name("Ibuprofeno 400mg")
                .description("Antiinflamatorio")
                .price(new Money(new BigDecimal("15000.00")))
                .stock(50)
                .build();
    }

    @Test
    @DisplayName("Should search by name and call async logging")
    void shouldSearchByNameAndCallAsyncLogging() {
        // Given: Mock repository returns results
        String query = "Acetaminofén";
        List<Product> expectedResults = List.of(testProduct1);
        when(productRepository.findByNameContaining(query)).thenReturn(expectedResults);

        // Act
        List<Product> results = productService.searchByName(query);

        // Assert: Search results returned
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(testProduct1);

        // Assert: Repository called with trimmed query
        verify(productRepository, times(1)).findByNameContaining(query);

        // Assert: Async logging called with query and result count
        verify(searchLogService, times(1)).logSearchAsync(query, 1);
    }

    @Test
    @DisplayName("Should handle empty search query")
    void shouldHandleEmptySearchQuery() {
        // When: Empty query provided
        List<Product> resultsNull = productService.searchByName(null);
        List<Product> resultsEmpty = productService.searchByName("");
        List<Product> resultsBlank = productService.searchByName("   ");

        // Then: Return empty list
        assertThat(resultsNull).isEmpty();
        assertThat(resultsEmpty).isEmpty();
        assertThat(resultsBlank).isEmpty();

        // Verify repository was never called
        verify(productRepository, never()).findByNameContaining(anyString());

        // Verify async logging was never called
        verify(searchLogService, never()).logSearchAsync(anyString(), anyInt());
    }

    @Test
    @DisplayName("Should trim query before searching")
    void shouldTrimQueryBeforeSearching() {
        // Given: Query with spaces
        String queryWithSpaces = "  Acetaminofén  ";
        String trimmedQuery = "Acetaminofén";
        when(productRepository.findByNameContaining(trimmedQuery)).thenReturn(List.of(testProduct1));

        // Act
        productService.searchByName(queryWithSpaces);

        // Assert: Repository called with trimmed query
        verify(productRepository, times(1)).findByNameContaining(trimmedQuery);
    }

    @Test
    @DisplayName("Should log search even when no results found")
    void shouldLogSearchEvenWhenNoResultsFound() {
        // Given: Search returns no results
        String query = "NonexistentProduct";
        when(productRepository.findByNameContaining(query)).thenReturn(List.of());

        // Act
        List<Product> results = productService.searchByName(query);

        // Assert: Empty results
        assertThat(results).isEmpty();

        // Assert: Async logging still called with 0 results
        verify(searchLogService, times(1)).logSearchAsync(query, 0);
    }

    @Test
    @DisplayName("Should find product by ID")
    void shouldFindProductById() {
        // Given: Product exists
        UUID productId = testProduct1.getId();
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct1));

        // Act
        Optional<Product> result = productService.findById(productId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testProduct1);
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should return empty when product not found by ID")
    void shouldReturnEmptyWhenProductNotFoundById() {
        // Given: Product doesn't exist
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productService.findById(productId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find all products in stock")
    void shouldFindAllProductsInStock() {
        // Given: Products in stock
        List<Product> inStockProducts = List.of(testProduct1, testProduct2);
        when(productRepository.findAllInStock()).thenReturn(inStockProducts);

        // Act
        List<Product> results = productService.findAllInStock();

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results).containsExactly(testProduct1, testProduct2);
        verify(productRepository, times(1)).findAllInStock();
    }

    @Test
    @DisplayName("Should find all products")
    void shouldFindAllProducts() {
        // Given: All products
        List<Product> allProducts = List.of(testProduct1, testProduct2);
        when(productRepository.findAll()).thenReturn(allProducts);

        // Act
        List<Product> results = productService.findAll();

        // Assert
        assertThat(results).hasSize(2);
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should check if product has sufficient stock")
    void shouldCheckIfProductHasSufficientStock() {
        // Given: Product with 100 units
        UUID productId = testProduct1.getId();
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct1));

        // Act & Assert: Check for 50 units (should have stock)
        boolean hasSufficientStock = productService.hasStock(productId, 50);
        assertThat(hasSufficientStock).isTrue();

        // Act & Assert: Check for 150 units (insufficient stock)
        boolean hasInsufficientStock = productService.hasStock(productId, 150);
        assertThat(hasInsufficientStock).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when checking stock for non-existent product")
    void shouldThrowExceptionWhenCheckingStockForNonExistentProduct() {
        // Given: Product doesn't exist
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.hasStock(productId, 10))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should handle search with multiple results")
    void shouldHandleSearchWithMultipleResults() {
        // Given: Search returns multiple products
        String query = "fen"; // Matches both Acetaminofén and Ibuprofeno
        List<Product> multipleResults = List.of(testProduct1, testProduct2);
        when(productRepository.findByNameContaining(query)).thenReturn(multipleResults);

        // Act
        List<Product> results = productService.searchByName(query);

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results).contains(testProduct1, testProduct2);

        // Verify async logging called with correct count
        verify(searchLogService, times(1)).logSearchAsync(query, 2);
    }

    @Test
    @DisplayName("Should call async logging service")
    void shouldCallAsyncLoggingService() {
        // Given: Search returns results
        String query = "Acetaminofén";
        when(productRepository.findByNameContaining(query)).thenReturn(List.of(testProduct1));
        doNothing().when(searchLogService).logSearchAsync(anyString(), anyInt());

        // Act
        List<Product> results = productService.searchByName(query);

        // Assert: Main flow completes and async logging called
        assertThat(results).hasSize(1);
        verify(productRepository, times(1)).findByNameContaining(query);
        verify(searchLogService, times(1)).logSearchAsync(query, 1);

        // Note: In production with @Async, if logging fails, it won't affect the main flow
        // The @Async method runs in a separate thread and exceptions are handled by Spring
    }

    @Test
    @DisplayName("Should handle product with exact stock match")
    void shouldHandleProductWithExactStockMatch() {
        // Given: Product with exactly 100 units
        UUID productId = testProduct1.getId();
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct1));

        // Act: Check for exactly 100 units
        boolean hasExactStock = productService.hasStock(productId, 100);

        // Assert: Should have sufficient stock
        assertThat(hasExactStock).isTrue();
    }

    @Test
    @DisplayName("Should handle case-sensitive search query")
    void shouldHandleCaseSensitiveSearchQuery() {
        // Given: Repository handles case-insensitive search
        String upperCaseQuery = "ACETAMINOFÉN";
        when(productRepository.findByNameContaining(upperCaseQuery)).thenReturn(List.of(testProduct1));

        // Act
        List<Product> results = productService.searchByName(upperCaseQuery);

        // Assert
        assertThat(results).hasSize(1);
        verify(productRepository, times(1)).findByNameContaining(upperCaseQuery);
        verify(searchLogService, times(1)).logSearchAsync(upperCaseQuery, 1);
    }
}
