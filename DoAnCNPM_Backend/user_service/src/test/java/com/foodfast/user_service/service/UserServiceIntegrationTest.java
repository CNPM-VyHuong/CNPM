package com.foodfast.user_service.service;

import com.foodfast.user_service.model.Role;
import com.foodfast.user_service.model.User;
import com.foodfast.user_service.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("UserService Integration Tests")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    @DisplayName("Should create user and persist to database")
    void testCreateUser() {
        User user = User.builder()
                .fullname("John Doe")
                .email("john@test.com")
                .phone("1234567890")
                .password("password123")
                .role(Role.CUSTOMER)
                .build();

        User created = userService.createUser(user);

        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        assertEquals("John Doe", created.getFullname());
        assertEquals("john@test.com", created.getEmail());

        User found = userRepository.findById(created.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("John Doe", found.getFullname());
    }

    @Test
    @Transactional
    @DisplayName("Should get all users from database")
    void testGetAllUsers() {
        User user1 = User.builder()
                .fullname("User One")
                .email("user1@test.com")
                .phone("1111111111")
                .password("pass1")
                .role(Role.CUSTOMER)
                .build();

        User user2 = User.builder()
                .fullname("User Two")
                .email("user2@test.com")
                .phone("2222222222")
                .password("pass2")
                .role(Role.RESTAURANT_OWNER)
                .build();

        userRepository.saveAll(List.of(user1, user2));

        List<User> users = userService.getAllUsers();

        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("user1@test.com")));
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("user2@test.com")));
    }

    @Test
    @Transactional
    @DisplayName("Should get user by ID from database")
    void testGetUserById() {
        User user = User.builder()
                .fullname("Test User")
                .email("test@test.com")
                .phone("9999999999")
                .password("testpass")
                .role(Role.CUSTOMER)
                .build();

        User saved = userRepository.save(user);

        Optional<User> found = userService.getUserById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getFullname());
        assertEquals("test@test.com", found.get().getEmail());
    }

    @Test
    @Transactional
    @DisplayName("Should get user by email from database")
    void testGetUserByEmail() {
        User user = User.builder()
                .fullname("Email Test")
                .email("email@test.com")
                .phone("3333333333")
                .password("emailpass")
                .role(Role.CUSTOMER)
                .build();

        userRepository.save(user);

        Optional<User> found = userService.getUserByEmail("email@test.com");

        assertTrue(found.isPresent());
        assertEquals("Email Test", found.get().getFullname());
    }

    @Test
    @Transactional
    @DisplayName("Should update user information in database")
    void testUpdateUser() {
        User user = User.builder()
                .fullname("Old Name")
                .email("update@test.com")
                .phone("4444444444")
                .password("oldpass")
                .role(Role.CUSTOMER)
                .build();

        User saved = userRepository.save(user);

        User updatedData = User.builder()
                .fullname("New Name")
                .email("newemail@test.com")
                .phone("5555555555")
                .password("newpass")
                .role(Role.RESTAURANT_OWNER)
                .build();

        User updated = userService.updateUser(saved.getId(), updatedData);

        assertEquals("New Name", updated.getFullname());
        assertEquals("newemail@test.com", updated.getEmail());
        assertEquals("5555555555", updated.getPhone());
        assertEquals(Role.RESTAURANT_OWNER, updated.getRole());

        User verified = userRepository.findById(saved.getId()).orElse(null);
        assertNotNull(verified);
        assertEquals("New Name", verified.getFullname());
    }

    @Test
    @Transactional
    @DisplayName("Should delete user from database")
    void testDeleteUser() {
        User user = User.builder()
                .fullname("Delete Me")
                .email("delete@test.com")
                .phone("6666666666")
                .password("deletepass")
                .role(Role.CUSTOMER)
                .build();

        User saved = userRepository.save(user);
        assertTrue(userRepository.findById(saved.getId()).isPresent());

        userService.deleteUser(saved.getId());

        assertFalse(userRepository.findById(saved.getId()).isPresent());
    }

    @Test
    @Transactional
    @DisplayName("Should prevent duplicate email addresses")
    void testPreventDuplicateEmail() {
        User user1 = User.builder()
                .fullname("User One")
                .email("test1@test.com")
                .phone("7777777777")
                .password("pass1")
                .role(Role.CUSTOMER)
                .build();
        User savedUser1 = userService.createUser(user1);

        User user2 = User.builder()
                .fullname("User Two")
                .email("test2@test.com")
                .phone("8888888888")
                .password("pass2")
                .role(Role.CUSTOMER)
                .build();
        userService.createUser(user2);

        // Try to update user1 with user2's email - should fail with constraint violation
        User updateAttempt = User.builder()
                .email("test2@test.com")
                .build();

        assertThrows(RuntimeException.class, () -> {
            userService.updateUser(savedUser1.getId(), updateAttempt);
        });
    }

    @Test
    @Transactional
    @DisplayName("Should encode password on user creation")
    void testPasswordEncoding() {
        User user = User.builder()
                .fullname("Password Test")
                .email("passtest@test.com")
                .phone("1010101010")
                .password("plainpassword")
                .role(Role.CUSTOMER)
                .build();

        User created = userService.createUser(user);
        User saved = userRepository.findById(created.getId()).orElse(null);

        assertNotNull(saved);
        assertNotEquals("plainpassword", saved.getPassword());
        assertTrue(saved.getPassword().length() > 10);
    }
}
