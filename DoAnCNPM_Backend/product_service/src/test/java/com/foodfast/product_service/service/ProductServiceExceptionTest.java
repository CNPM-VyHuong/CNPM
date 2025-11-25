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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Exception Handling Tests")
public class ProductServiceExceptionTest {

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
    }

    @Test
    @DisplayName("Should handle null product gracefully")
    void testCreateNullProduct() {
        // Service might allow null, so we test that repository.save is called
        when(productRepository.save(any())).thenReturn(testProduct);
        
        productService.createProduct(null);
        
        verify(productRepository).save(null);
    }

    @Test
    @DisplayName("Should handle repository exception on findById")
    void testFindByIdRepositoryException() {
        when(productRepository.findById(any())).thenThrow(
            new RuntimeException("Database connection failed")
        );

        assertThatThrownBy(() -> productService.getProductById(1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Database connection failed");
    }

    @Test
    @DisplayName("Should handle repository exception on save")
    void testSaveRepositoryException() {
        when(productRepository.save(any(Product.class))).thenThrow(
            new RuntimeException("Failed to save product")
        );

        assertThatThrownBy(() -> productService.createProduct(testProduct))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to save product");
    }

    @Test
    @DisplayName("Should handle non-existent product on update")
    void testUpdateNonExistentProduct() {
        when(productRepository.existsById(999L)).thenReturn(false);

        Product result = productService.updateProduct(999L, testProduct);

        assertThat(result).isNull();
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle repository exception on delete")
    void testDeleteRepositoryException() {
        doThrow(new RuntimeException("Failed to delete product"))
            .when(productRepository).deleteById(any());

        assertThatThrownBy(() -> productService.deleteProduct(1L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to delete product");
    }

    @Test
    @DisplayName("Should handle getAllProducts exception")
    void testGetAllProductsException() {
        when(productRepository.findAll()).thenThrow(
            new RuntimeException("Database query failed")
        );

        assertThatThrownBy(() -> productService.getAllProducts())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Database query failed");
    }

    @Test
    @DisplayName("Should return Optional.empty when product not found")
    void testGetProductByIdNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should verify delete is called with correct id")
    void testDeleteProductVerification() {
        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should verify save is called on update existing product")
    void testUpdateExistingProductVerification() {
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        productService.updateProduct(1L, testProduct);

        verify(productRepository).existsById(1L);
        verify(productRepository).save(any(Product.class));
    }
}
