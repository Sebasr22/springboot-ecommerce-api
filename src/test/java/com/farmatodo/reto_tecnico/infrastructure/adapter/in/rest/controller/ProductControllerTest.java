package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.controller;

import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import com.farmatodo.reto_tecnico.domain.port.in.SearchProductUseCase;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper.ProductRestMapperImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for ProductController.
 * Tests product search with pagination and query parameters.
 *
 * Uses ProductRestMapperImpl for DTO conversion.
 */
@WebMvcTest(ProductController.class)
@Import(ProductRestMapperImpl.class)
@DisplayName("ProductController REST Tests")
class ProductControllerTest {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_KEY_VALUE = "default-dev-key-change-in-production";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SearchProductUseCase searchProductUseCase;

    private List<Product> testProducts;

    @BeforeEach
    void setUp() {
        testProducts = new ArrayList<>();

        Product product1 = Product.builder()
                .id(UUID.randomUUID())
                .name("Acetaminofén 500mg")
                .description("Analgésico y antipirético")
                .price(new Money(new BigDecimal("10000.00")))
                .stock(100)
                .build();

        Product product2 = Product.builder()
                .id(UUID.randomUUID())
                .name("Ibuprofeno 400mg")
                .description("Antiinflamatorio")
                .price(new Money(new BigDecimal("15000.00")))
                .stock(50)
                .build();

        Product product3 = Product.builder()
                .id(UUID.randomUUID())
                .name("Acetaminofén + Cafeína")
                .description("Analgésico con cafeína")
                .price(new Money(new BigDecimal("12000.00")))
                .stock(75)
                .build();

        testProducts.add(product1);
        testProducts.add(product2);
        testProducts.add(product3);
    }

    @Test
    @DisplayName("Should search products by name with 200 status")
    void shouldSearchProducts() throws Exception {
        // Given: Search query for "Acetaminofén"
        List<Product> searchResults = List.of(testProducts.get(0), testProducts.get(2));
        when(searchProductUseCase.searchByName("Acetaminofén")).thenReturn(searchResults);

        // When & Then: Call endpoint and verify response
        mockMvc.perform(get("/api/v1/products")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("search", "Acetaminofén"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Acetaminofén 500mg"))
                .andExpect(jsonPath("$[0].price").value(10000.0))
                .andExpect(jsonPath("$[0].stock").value(100))
                .andExpect(jsonPath("$[1].name").value("Acetaminofén + Cafeína"));

        // Verify service was called with correct query
        verify(searchProductUseCase, times(1)).searchByName("Acetaminofén");
        verify(searchProductUseCase, never()).findAllInStock();
        verify(searchProductUseCase, never()).findAll();
    }

    @Test
    @DisplayName("Should return all in-stock products when no search query provided")
    void shouldReturnAllInStockProductsWhenNoQuery() throws Exception {
        // Given: No search query
        when(searchProductUseCase.findAllInStock()).thenReturn(testProducts);

        // When & Then: Call endpoint without search param
        mockMvc.perform(get("/api/v1/products")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name").value("Acetaminofén 500mg"))
                .andExpect(jsonPath("$[1].name").value("Ibuprofeno 400mg"))
                .andExpect(jsonPath("$[2].name").value("Acetaminofén + Cafeína"));

        // Verify correct method was called
        verify(searchProductUseCase, times(1)).findAllInStock();
        verify(searchProductUseCase, never()).searchByName(anyString());
    }

    @Test
    @DisplayName("Should handle pagination with page and size parameters")
    void shouldHandlePaginationParameters() throws Exception {
        // Given: Multiple products with pagination
        when(searchProductUseCase.findAllInStock()).thenReturn(testProducts);

        // When & Then: Request page 0 with size 2
        mockMvc.perform(get("/api/v1/products")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Acetaminofén 500mg"))
                .andExpect(jsonPath("$[1].name").value("Ibuprofeno 400mg"));

        verify(searchProductUseCase, times(1)).findAllInStock();
    }

    @Test
    @DisplayName("Should handle pagination page 1")
    void shouldHandlePaginationPage1() throws Exception {
        // Given: Multiple products
        when(searchProductUseCase.findAllInStock()).thenReturn(testProducts);

        // When & Then: Request page 1 with size 2 (should show 3rd product)
        mockMvc.perform(get("/api/v1/products")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Acetaminofén + Cafeína"));

        verify(searchProductUseCase, times(1)).findAllInStock();
    }

    @Test
    @DisplayName("Should return empty array when page exceeds available results")
    void shouldReturnEmptyArrayWhenPageExceedsResults() throws Exception {
        // Given: 3 products total
        when(searchProductUseCase.findAllInStock()).thenReturn(testProducts);

        // When & Then: Request page 5 (exceeds available data)
        mockMvc.perform(get("/api/v1/products")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("page", "5")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(searchProductUseCase, times(1)).findAllInStock();
    }

    @Test
    @DisplayName("Should use default pagination values when not specified")
    void shouldUseDefaultPaginationValues() throws Exception {
        // Given: Products available
        when(searchProductUseCase.findAllInStock()).thenReturn(testProducts);

        // When & Then: Request without pagination params (defaults: page=0, size=20)
        mockMvc.perform(get("/api/v1/products")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))); // All 3 products fit in default size

        verify(searchProductUseCase, times(1)).findAllInStock();
    }

    @Test
    @DisplayName("Should search with query and apply pagination")
    void shouldSearchWithQueryAndPagination() throws Exception {
        // Given: Search returns multiple results
        List<Product> searchResults = List.of(testProducts.get(0), testProducts.get(2));
        when(searchProductUseCase.searchByName("Acetaminofén")).thenReturn(searchResults);

        // When & Then: Search with pagination
        mockMvc.perform(get("/api/v1/products")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("search", "Acetaminofén")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Acetaminofén 500mg"));

        verify(searchProductUseCase, times(1)).searchByName("Acetaminofén");
    }

    @Test
    @DisplayName("Should return empty array when search finds no results")
    void shouldReturnEmptyArrayWhenSearchFindsNothing() throws Exception {
        // Given: Search returns no results
        when(searchProductUseCase.searchByName("NoExiste")).thenReturn(List.of());

        // When & Then: Call endpoint
        mockMvc.perform(get("/api/v1/products")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("search", "NoExiste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(searchProductUseCase, times(1)).searchByName("NoExiste");
    }

    @Test
    @DisplayName("Should get all products including out-of-stock")
    void shouldGetAllProductsIncludingOutOfStock() throws Exception {
        // Given: All products (including out-of-stock)
        Product outOfStockProduct = Product.builder()
                .id(UUID.randomUUID())
                .name("Dipirona")
                .description("Analgésico")
                .price(new Money(new BigDecimal("8000.00")))
                .stock(0)
                .build();

        List<Product> allProducts = new ArrayList<>(testProducts);
        allProducts.add(outOfStockProduct);

        when(searchProductUseCase.findAll()).thenReturn(allProducts);

        // When & Then: Call /all endpoint
        mockMvc.perform(get("/api/v1/products/all")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[3].name").value("Dipirona"))
                .andExpect(jsonPath("$[3].stock").value(0));

        verify(searchProductUseCase, times(1)).findAll();
        verify(searchProductUseCase, never()).findAllInStock();
    }

    @Test
    @DisplayName("Should return 200 OK even with empty results")
    void shouldReturn200EvenWithEmptyResults() throws Exception {
        // Given: No products available
        when(searchProductUseCase.findAllInStock()).thenReturn(List.of());

        // When & Then: Still returns 200 with empty array
        mockMvc.perform(get("/api/v1/products")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(searchProductUseCase, times(1)).findAllInStock();
    }

    @Test
    @DisplayName("Should handle blank search query as no query")
    void shouldHandleBlankSearchQueryAsNoQuery() throws Exception {
        // Given: Blank search parameter
        when(searchProductUseCase.findAllInStock()).thenReturn(testProducts);

        // When & Then: Blank query should trigger findAllInStock (not searchByName)
        mockMvc.perform(get("/api/v1/products")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .param("search", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        verify(searchProductUseCase, times(1)).findAllInStock();
        verify(searchProductUseCase, never()).searchByName(anyString());
    }

    @Test
    @DisplayName("Should include product details in response")
    void shouldIncludeProductDetailsInResponse() throws Exception {
        // Given: Single product
        when(searchProductUseCase.findAllInStock()).thenReturn(List.of(testProducts.get(0)));

        // When & Then: Verify all product fields are present
        mockMvc.perform(get("/api/v1/products")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").value("Acetaminofén 500mg"))
                .andExpect(jsonPath("$[0].description").value("Analgésico y antipirético"))
                .andExpect(jsonPath("$[0].price").value(10000.0))
                .andExpect(jsonPath("$[0].stock").value(100));

        verify(searchProductUseCase, times(1)).findAllInStock();
    }
}
