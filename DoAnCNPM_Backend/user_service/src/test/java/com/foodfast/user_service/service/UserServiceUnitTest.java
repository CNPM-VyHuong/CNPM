package com.foodfast.user_service.service;

import com.foodfast.user_service.model.Role;
import com.foodfast.user_service.model.User;
import com.foodfast.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("UserService Unit Tests")
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = User.builder()
                .id(1L)
                .fullname("John Doe")
                .email("john@example.com")
                .phone("1234567890")
                .password("encoded_password")
                .role(Role.CUSTOMER)
                .build();
    }

    // ============ GET USERS TESTS ============

    @Test
    @DisplayName("Should get all users successfully")
    void testGetAllUsersSuccess() {
        // Arrange
        User user2 = User.builder()
                .id(2L)
                .fullname("Jane Smith")
                .email("jane@example.com")
                .role(Role.RESTAURANT_OWNER)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(testUser, user2));

        // Act
        List<User> users = userService.getAllUsers();

        // Assert
        assertEquals(2, users.size());
        assertEquals("john@example.com", users.get(0).getEmail());
        assertEquals("jane@example.com", users.get(1).getEmail());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should handle empty user list")
    void testGetAllUsersEmpty() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of());

        // Act
        List<User> users = userService.getAllUsers();

        // Assert
        assertTrue(users.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    // ============ GET USER BY ID TESTS ============

    @Test
    @DisplayName("Should get user by ID successfully")
    void testGetUserByIdSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> user = userService.getUserById(1L);

        // Assert
        assertTrue(user.isPresent());
        assertEquals("john@example.com", user.get().getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty Optional when user not found")
    void testGetUserByIdNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<User> user = userService.getUserById(999L);

        // Assert
        assertTrue(user.isEmpty());
        verify(userRepository, times(1)).findById(999L);
    }

    // ============ GET USER BY EMAIL TESTS ============

    @Test
    @DisplayName("Should get user by email successfully")
    void testGetUserByEmailSuccess() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> user = userService.getUserByEmail("john@example.com");

        // Assert
        assertTrue(user.isPresent());
        assertEquals("John Doe", user.get().getFullname());
        verify(userRepository, times(1)).findByEmail("john@example.com");
    }

    @Test
    @DisplayName("Should return empty Optional when email not found")
    void testGetUserByEmailNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        Optional<User> user = userService.getUserByEmail("nonexistent@example.com");

        // Assert
        assertTrue(user.isEmpty());
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    // ============ CREATE USER TESTS ============

    @Test
    @DisplayName("Should create user successfully")
    void testCreateUserSuccess() {
        // Arrange
        User newUser = User.builder()
                .fullname("Alice Johnson")
                .email("alice@example.com")
                .phone("9876543210")
                .password("raw_password")
                .role(Role.CUSTOMER)
                .build();

        when(passwordEncoder.encode("raw_password")).thenReturn("encoded_password_123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User created = userService.createUser(newUser);

        // Assert
        assertNotNull(created);
        assertEquals("john@example.com", created.getEmail());
        verify(passwordEncoder, times(1)).encode("raw_password");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should encode password on user creation")
    void testCreateUserPasswordEncoded() {
        // Arrange
        User newUser = User.builder()
                .fullname("Bob")
                .email("bob@example.com")
                .password("plaintext")
                .role(Role.RESTAURANT_OWNER)
                .build();

        when(passwordEncoder.encode("plaintext")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User created = userService.createUser(newUser);

        // Assert
        assertEquals("hashed_password", created.getPassword());
        verify(passwordEncoder, times(1)).encode("plaintext");
    }

    // ============ UPDATE USER TESTS ============

    @Test
    @DisplayName("Should update user successfully")
    void testUpdateUserSuccess() {
        // Arrange
        User updatedData = User.builder()
                .fullname("John Updated")
                .email("john_new@example.com")
                .phone("1111111111")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("john_new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User updated = userService.updateUser(1L, updatedData);

        // Assert
        assertNotNull(updated);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating with duplicate email")
    void testUpdateUserDuplicateEmail() {
        // Arrange
        User existingUser = User.builder()
                .id(2L)
                .email("existing@example.com")
                .build();

        User updatedData = User.builder()
                .email("existing@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.updateUser(1L, updatedData);
        });
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when user not found for update")
    void testUpdateUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.updateUser(999L, new User());
        });
    }

    @Test
    @DisplayName("Should update password successfully")
    void testUpdateUserPasswordSuccess() {
        // Arrange
        User updatedData = User.builder()
                .password("new_raw_password")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("new_raw_password")).thenReturn("new_encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User updated = userService.updateUser(1L, updatedData);

        // Assert
        assertNotNull(updated);
        verify(passwordEncoder, times(1)).encode("new_raw_password");
    }

    @Test
    @DisplayName("Should not update password if blank")
    void testUpdateUserPasswordBlank() {
        // Arrange
        User updatedData = User.builder()
                .fullname("John Updated")
                .password("")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.updateUser(1L, updatedData);

        // Assert
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ============ DELETE USER TESTS ============

    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUserSuccess() {
        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should handle delete with non-existent ID")
    void testDeleteUserNonExistent() {
        // Act
        userService.deleteUser(999L);

        // Assert
        verify(userRepository, times(1)).deleteById(999L);
    }

    // ============ BUSINESS LOGIC TESTS ============

    @Test
    @DisplayName("Should handle role assignment correctly")
    void testUserRoleAssignment() {
        // Arrange
        User adminUser = User.builder()
                .id(1L)
                .fullname("Admin User")
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        // Act
        assertEquals(Role.ADMIN, adminUser.getRole());
    }

    @Test
    @DisplayName("Should validate user data integrity")
    void testUserDataIntegrity() {
        // Arrange & Act
        assertNotNull(testUser.getId());
        assertNotNull(testUser.getEmail());
        assertNotNull(testUser.getPassword());
        assertNotNull(testUser.getRole());

        // Assert
        assertEquals(1L, testUser.getId());
        assertEquals("john@example.com", testUser.getEmail());
        assertEquals(Role.CUSTOMER, testUser.getRole());
    }
}
