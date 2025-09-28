package com.example.demo1.service;

import com.example.demo1.model.Role;
import com.example.demo1.model.User;
import com.example.demo1.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // Test 1: Valid email format should register user successfully
    @Test
    void registerUser_ValidEmail_UserCreatedSuccessfully() {
        // Given
        User user = new User();
        user.setName("John Doe");
        user.setEmail("test@example.com");
        user.setPassword("Password123!");
        user.setRole(Role.USER);

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        User result = userService.registerUser(user);

        // Then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    // Test 2: Invalid email format should throw exception
    @Test
    void registerUser_InvalidEmail_ThrowsValidationException() {
        // Given
        User user = new User();
        user.setName("John Doe");
        user.setEmail("invalid-email"); // Invalid email format
        user.setPassword("Password123!");
        user.setRole(Role.USER);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(user);
        });
    }

    // Test 3: Duplicate email should throw exception
    @Test
    void registerUser_DuplicateEmail_ThrowsException() {
        // Given
        User user = new User();
        user.setName("John Doe");
        user.setEmail("existing@example.com");
        user.setPassword("Password123!");
        user.setRole(Role.USER);

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(user);
        });
        assertTrue(exception.getMessage().contains("already exists"));
    }



    // Test 4: Email validation method works correctly
    @Test
    void userExists_ValidEmail_ReturnsTrue() {
        // Given
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When
        boolean result = userService.userExists(email);

        // Then
        assertTrue(result);
    }

    // Test 5: Email validation method returns false for non-existent email
    @Test
    void userExists_NonExistentEmail_ReturnsFalse() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // When
        boolean result = userService.userExists(email);

        // Then
        assertFalse(result);
    }
}