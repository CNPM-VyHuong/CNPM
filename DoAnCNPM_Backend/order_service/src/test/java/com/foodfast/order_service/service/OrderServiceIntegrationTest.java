package com.foodfast.order_service.service;

import com.foodfast.order_service.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Test
    @Transactional
    void testCreateAndRetrieveOrder() {
        Order order = Order.builder()
                .userId("user1")
                .fullname("Nguyen Van A")
                .phone("0123456789")
                .address("123 Street")
                .total(BigDecimal.valueOf(200))
                .status(0)
                .build();

        Order savedOrder = orderService.createOrder(order);
        assertNotNull(savedOrder.getId());

        Order retrieved = orderService.getOrderById(savedOrder.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals("Nguyen Van A", retrieved.getFullname());
    }
}
