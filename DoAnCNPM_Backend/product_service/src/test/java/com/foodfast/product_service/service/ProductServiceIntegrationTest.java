package com.foodfast.product_service.service;

import com.foodfast.product_service.model.Product;
import com.foodfast.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Integration Tests")
public class ProductServiceIntegrationTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Burger");
        testProduct.setPrice(new BigDecimal("5.99"));
        testProduct.setQuantity(new BigDecimal("100"));
        testProduct.setCreatedAt(Instant.now());
    }

    @Test
    @DisplayName("Should create product successfully")
    void testCreateProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.createProduct(testProduct);

        assertNotNull(result);
        assertEquals("Burger", result.getName());
        assertEquals(new BigDecimal("5.99"), result.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should get all products successfully")
    void testGetAllProducts() {
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Pizza");
        product2.setPrice(new BigDecimal("8.99"));
        product2.setQuantity(new BigDecimal("50"));

        List<Product> expectedProducts = Arrays.asList(testProduct, product2);
        when(productRepository.findAll()).thenReturn(expectedProducts);

        List<Product> result = productService.getAllProducts();

        assertEquals(2, result.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void testGetProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Optional<Product> result = productService.getProductById(1L);

        assertTrue(result.isPresent());
        assertEquals("Burger", result.get().getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProduct() {
        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Burger");
        updatedProduct.setPrice(new BigDecimal("6.99"));
        updatedProduct.setQuantity(new BigDecimal("120"));

        when(productRepository.existsById(1L)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        Product result = productService.updateProduct(1L, updatedProduct);

        assertNotNull(result);
        assertEquals("Updated Burger", result.getName());
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProduct() {
        productService.deleteProduct(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should handle product with zero quantity")
    void testProductWithZeroQuantity() {
        Product zeroQtyProduct = new Product();
        zeroQtyProduct.setId(3L);
        zeroQtyProduct.setName("Out of Stock");
        zeroQtyProduct.setPrice(new BigDecimal("10.00"));
        zeroQtyProduct.setQuantity(new BigDecimal("0"));

        when(productRepository.save(any(Product.class))).thenReturn(zeroQtyProduct);

        Product result = productService.createProduct(zeroQtyProduct);

        assertEquals(new BigDecimal("0"), result.getQuantity());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should handle product not found")
    void testGetProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(999L);

        assertFalse(result.isPresent());
        verify(productRepository, times(1)).findById(999L);
    }
}

