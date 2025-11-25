package com.foodfast.order_service.service;

import com.foodfast.order_service.model.Order;
import com.foodfast.order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceUnitTest {

    private OrderRepository orderRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderService = new OrderService(orderRepository);
    }

    @Test
    void testCreateOrder() {
        Order order = new Order();
        order.setUserId("user1");
        order.setTotal(BigDecimal.valueOf(100));

        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.createOrder(order);
        assertNotNull(result);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testUpdateOrderStatus() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(0);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order updated = orderService.updateOrderStatus(1L, 2);
        assertEquals(2, updated.getStatus());
    }
}
