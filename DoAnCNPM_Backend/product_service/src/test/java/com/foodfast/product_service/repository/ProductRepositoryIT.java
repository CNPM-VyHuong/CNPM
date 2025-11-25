package com.foodfast.product_service.repository;

import com.foodfast.product_service.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("ProductRepository Integration Tests with PostgreSQL")
public class ProductRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test_product_db")
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        testProduct1 = new Product();
        testProduct1.setName("Burger");
        testProduct1.setPrice(new BigDecimal("5.99"));
        testProduct1.setQuantity(new BigDecimal("100"));

        testProduct2 = new Product();
        testProduct2.setName("Pizza");
        testProduct2.setPrice(new BigDecimal("8.99"));
        testProduct2.setQuantity(new BigDecimal("50"));
    }

    @Test
    @DisplayName("Should save and retrieve product from real PostgreSQL")
    void testSaveAndRetrieveProduct() {
        Product savedProduct = productRepository.save(testProduct1);

        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("Burger");
        assertThat(savedProduct.getPrice()).isEqualTo(new BigDecimal("5.99"));
        assertThat(savedProduct.getCreatedAt()).isNotNull();

        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Burger");
    }

    @Test
    @DisplayName("Should find all products from PostgreSQL")
    void testFindAllProducts() {
        productRepository.save(testProduct1);
        productRepository.save(testProduct2);

        List<Product> allProducts = productRepository.findAll();

        assertThat(allProducts).hasSize(2);
        assertThat(allProducts).extracting("name").contains("Burger", "Pizza");
    }

    @Test
    @DisplayName("Should update product in PostgreSQL")
    void testUpdateProduct() {
        Product savedProduct = productRepository.save(testProduct1);

        savedProduct.setName("Updated Burger");
        savedProduct.setPrice(new BigDecimal("6.99"));
        Product updatedProduct = productRepository.save(savedProduct);

        Product foundProduct = productRepository.findById(updatedProduct.getId()).orElse(null);
        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.getName()).isEqualTo("Updated Burger");
        assertThat(foundProduct.getPrice()).isEqualTo(new BigDecimal("6.99"));
    }

    @Test
    @DisplayName("Should delete product from PostgreSQL")
    void testDeleteProduct() {
        Product savedProduct = productRepository.save(testProduct1);
        Long productId = savedProduct.getId();

        productRepository.deleteById(productId);

        Optional<Product> deletedProduct = productRepository.findById(productId);
        assertThat(deletedProduct).isEmpty();
    }

    @Test
    @DisplayName("Should count products in PostgreSQL")
    void testCountProducts() {
        productRepository.save(testProduct1);
        productRepository.save(testProduct2);

        long count = productRepository.count();
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should check if product exists in PostgreSQL")
    void testExistsById() {
        Product savedProduct = productRepository.save(testProduct1);

        boolean exists = productRepository.existsById(savedProduct.getId());
        boolean notExists = productRepository.existsById(999L);

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should handle concurrent saves to PostgreSQL")
    void testConcurrentSaves() {
        Product product1 = productRepository.save(testProduct1);
        Product product2 = productRepository.save(testProduct2);

        assertThat(product1.getId()).isNotEqualTo(product2.getId());
        assertThat(productRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should persist BigDecimal values correctly")
    void testBigDecimalPersistence() {
        testProduct1.setPrice(new BigDecimal("123.45"));
        testProduct1.setQuantity(new BigDecimal("999.99"));

        Product savedProduct = productRepository.save(testProduct1);
        Product foundProduct = productRepository.findById(savedProduct.getId()).orElse(null);

        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.getPrice()).isEqualTo(new BigDecimal("123.45"));
        assertThat(foundProduct.getQuantity()).isEqualTo(new BigDecimal("999.99"));
    }

    @Test
    @DisplayName("Should persist timestamps correctly")
    void testTimestampPersistence() {
        Product savedProduct = productRepository.save(testProduct1);

        assertThat(savedProduct.getCreatedAt()).isNotNull();
        assertThat(savedProduct.getUpdatedAt()).isNotNull();

        Product foundProduct = productRepository.findById(savedProduct.getId()).orElse(null);
        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.getCreatedAt()).isNotNull();
        assertThat(foundProduct.getUpdatedAt()).isNotNull();
    }
}
