package com.foodfast.restaurant_service.service;

import com.foodfast.restaurant_service.client.UserClient;
import com.foodfast.restaurant_service.dto.UserDto;
import com.foodfast.restaurant_service.model.Restaurant;
import com.foodfast.restaurant_service.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantService Integration Tests")
public class RestaurantServiceIntegrationTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant testRestaurant;
    private UserDto testOwner;

    @BeforeEach
    void setUp() {
        testRestaurant = new Restaurant();
        testRestaurant.setId(1L);
        testRestaurant.setName("Pizza Palace");
        testRestaurant.setAddress("123 Main St");
        testRestaurant.setOwnerId(100L);
        testRestaurant.setOwnerEmail("owner@pizza.com");

        testOwner = new UserDto();
        testOwner.setId(100L);
        testOwner.setEmail("owner@pizza.com");
        testOwner.setRole("RESTAURANT_OWNER");
    }

    @Test
    @DisplayName("Should create restaurant successfully with valid owner")
    void testCreateRestaurant() {
        when(userClient.getUserByEmail("owner@pizza.com")).thenReturn(testOwner);
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(testRestaurant);

        Restaurant result = restaurantService.createRestaurant(testRestaurant);

        assertNotNull(result);
        assertEquals("Pizza Palace", result.getName());
        assertEquals(100L, result.getOwnerId());
        verify(userClient, times(1)).getUserByEmail("owner@pizza.com");
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    @DisplayName("Should fail to create restaurant with invalid owner")
    void testCreateRestaurantWithInvalidOwner() {
        UserDto invalidOwner = new UserDto();
        invalidOwner.setId(100L);
        invalidOwner.setEmail("owner@pizza.com");
        invalidOwner.setRole("CUSTOMER");

        when(userClient.getUserByEmail("owner@pizza.com")).thenReturn(invalidOwner);

        assertThrows(RuntimeException.class, () -> {
            restaurantService.createRestaurant(testRestaurant);
        });

        verify(userClient, times(1)).getUserByEmail("owner@pizza.com");
        verify(restaurantRepository, never()).save(any(Restaurant.class));
    }

    @Test
    @DisplayName("Should get restaurant by owner ID successfully")
    void testGetRestaurantByOwner() {
        when(restaurantRepository.findByOwnerId(100L)).thenReturn(Optional.of(testRestaurant));

        Restaurant result = restaurantService.getRestaurantByOwner(100L);

        assertNotNull(result);
        assertEquals("Pizza Palace", result.getName());
        assertEquals(100L, result.getOwnerId());
        verify(restaurantRepository, times(1)).findByOwnerId(100L);
    }

    @Test
    @DisplayName("Should throw exception when restaurant not found by owner ID")
    void testGetRestaurantByOwnerNotFound() {
        when(restaurantRepository.findByOwnerId(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            restaurantService.getRestaurantByOwner(999L);
        });

        verify(restaurantRepository, times(1)).findByOwnerId(999L);
    }

    @Test
    @DisplayName("Should get all restaurants successfully")
    void testGetAllRestaurants() {
        Restaurant restaurant2 = new Restaurant();
        restaurant2.setId(2L);
        restaurant2.setName("Burger Joint");
        restaurant2.setAddress("456 Oak Ave");
        restaurant2.setOwnerId(101L);
        restaurant2.setOwnerEmail("owner@burger.com");

        List<Restaurant> expectedRestaurants = Arrays.asList(testRestaurant, restaurant2);
        when(restaurantRepository.findAll()).thenReturn(expectedRestaurants);

        List<Restaurant> result = restaurantService.getAll();

        assertEquals(2, result.size());
        assertEquals("Pizza Palace", result.get(0).getName());
        assertEquals("Burger Joint", result.get(1).getName());
        verify(restaurantRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get restaurant by owner email successfully")
    void testGetRestaurantByOwnerEmail() {
        when(restaurantRepository.findByOwnerEmail("owner@pizza.com")).thenReturn(Optional.of(testRestaurant));

        Restaurant result = restaurantService.getRestaurantByOwnerEmail("owner@pizza.com");

        assertNotNull(result);
        assertEquals("Pizza Palace", result.getName());
        assertEquals("owner@pizza.com", result.getOwnerEmail());
        verify(restaurantRepository, times(1)).findByOwnerEmail("owner@pizza.com");
    }

    @Test
    @DisplayName("Should throw exception when restaurant not found by owner email")
    void testGetRestaurantByOwnerEmailNotFound() {
        when(restaurantRepository.findByOwnerEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            restaurantService.getRestaurantByOwnerEmail("nonexistent@example.com");
        });

        verify(restaurantRepository, times(1)).findByOwnerEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("Should handle user client connection failure gracefully")
    void testCreateRestaurantWithUserClientFailure() {
        when(userClient.getUserByEmail(anyString())).thenThrow(new RuntimeException("User service unavailable"));

        assertThrows(RuntimeException.class, () -> {
            restaurantService.createRestaurant(testRestaurant);
        });

        verify(userClient, times(1)).getUserByEmail("owner@pizza.com");
        verify(restaurantRepository, never()).save(any(Restaurant.class));
    }

    @Test
    @DisplayName("Should retrieve empty list when no restaurants exist")
    void testGetAllRestaurantsEmpty() {
        when(restaurantRepository.findAll()).thenReturn(Arrays.asList());

        List<Restaurant> result = restaurantService.getAll();

        assertTrue(result.isEmpty());
        verify(restaurantRepository, times(1)).findAll();
    }
}


