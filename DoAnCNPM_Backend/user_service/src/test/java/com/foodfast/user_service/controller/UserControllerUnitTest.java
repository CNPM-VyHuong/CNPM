package com.foodfast.user_service.controller;

import com.foodfast.user_service.dto.UserDTO;
import com.foodfast.user_service.model.Role;
import com.foodfast.user_service.model.User;
import com.foodfast.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("UserController Unit Tests")
class UserControllerUnitTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = User.builder()
                .id(1L)
                .fullname("John Doe")
                .email("john@example.com")
                .phone("1234567890")
                .role(Role.CUSTOMER)
                .build();
    }

    // ============ GET USERS TESTS ============

    @Test
    @DisplayName("Should get all users and return DTOs")
    void testGetAllUsers() {
        // Arrange
        User user2 = User.builder()
                .id(2L)
                .fullname("Jane Smith")
                .email("jane@example.com")
                .role(Role.RESTAURANT_OWNER)
                .build();

        when(userService.getAllUsers()).thenReturn(List.of(testUser, user2));

        // Act
        List<UserDTO> result = userController.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        assertEquals("john@example.com", result.get(0).getEmail());
        assertEquals("jane@example.com", result.get(1).getEmail());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("Should return empty list when no users exist")
    void testGetAllUsersEmpty() {
        // Arrange
        when(userService.getAllUsers()).thenReturn(List.of());

        // Act
        List<UserDTO> result = userController.getAllUsers();

        // Assert
        assertTrue(result.isEmpty());
        verify(userService, times(1)).getAllUsers();
    }

    // ============ GET USER BY ID TESTS ============

    @Test
    @DisplayName("Should get user by ID and return 200 OK")
    void testGetUserByIdSuccess() {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        // Act
        var response = userController.getUserById(1L);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals("john@example.com", response.getBody().getEmail());
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    @DisplayName("Should return 404 when user not found")
    void testGetUserByIdNotFound() {
        // Arrange
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // Act
        var response = userController.getUserById(999L);

        // Assert
        assertTrue(response.getStatusCode().is4xxClientError());
        assertTrue(response.getStatusCode().value() == 404);
    }

    // ============ CREATE USER TESTS ============

    @Test
    @DisplayName("Should create user and return DTO")
    void testCreateUser() {
        // Arrange
        User newUser = User.builder()
                .fullname("Alice Johnson")
                .email("alice@example.com")
                .build();

        when(userService.createUser(newUser)).thenReturn(testUser);

        // Act
        UserDTO result = userController.createUser(newUser);

        // Assert
        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
        verify(userService, times(1)).createUser(newUser);
    }

    // ============ UPDATE USER TESTS ============

    @Test
    @DisplayName("Should update user and return 200 OK")
    void testUpdateUser() {
        // Arrange
        User updatedUser = User.builder()
                .fullname("John Updated")
                .build();

        when(userService.updateUser(1L, updatedUser)).thenReturn(testUser);

        // Act
        var response = userController.updateUser(1L, updatedUser);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        verify(userService, times(1)).updateUser(1L, updatedUser);
    }

    // ============ DELETE USER TESTS ============

    @Test
    @DisplayName("Should delete user and return 204 No Content")
    void testDeleteUser() {
        // Act
        var response = userController.deleteUser(1L);

        // Assert
        assertTrue(response.getStatusCode().value() == 204);
        verify(userService, times(1)).deleteUser(1L);
    }

    // ============ GET USER BY EMAIL TESTS ============

    @Test
    @DisplayName("Should get user by email and return 200 OK")
    void testGetUserByEmailSuccess() {
        // Arrange
        when(userService.getUserByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // Act
        var response = userController.getUserByEmail("john@example.com");

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals("John Doe", response.getBody().getFullname());
        verify(userService, times(1)).getUserByEmail("john@example.com");
    }

    @Test
    @DisplayName("Should return 404 when email not found")
    void testGetUserByEmailNotFound() {
        // Arrange
        when(userService.getUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        var response = userController.getUserByEmail("nonexistent@example.com");

        // Assert
        assertTrue(response.getStatusCode().is4xxClientError());
    }

    // ============ DTO CONVERSION TESTS ============

    @Test
    @DisplayName("Should convert User entity to UserDTO correctly")
    void testUserToDTOConversion() {
        // Act
        UserDTO dto = new UserDTO(
                testUser.getId(),
                testUser.getFullname(),
                testUser.getEmail(),
                testUser.getPhone(),
                testUser.getRole()
        );

        // Assert
        assertEquals(testUser.getId(), dto.getId());
        assertEquals(testUser.getFullname(), dto.getFullname());
        assertEquals(testUser.getEmail(), dto.getEmail());
        assertEquals(testUser.getPhone(), dto.getPhone());
        assertEquals(testUser.getRole(), dto.getRole());
    }
}
