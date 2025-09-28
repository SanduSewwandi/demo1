package com.example.demo1.service;

import com.example.demo1.model.Role;
import com.example.demo1.model.User;
import com.example.demo1.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User validUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        validUser = new User();
        validUser.setId(1L);
        validUser.setName("Test User");
        validUser.setEmail("test@example.com");
        validUser.setPassword("Password123");
        validUser.setRole(Role.USER);
    }

    // ---------------- GREEN TESTS -------------------

    @Test
    @DisplayName("🟩 GREEN: Successful registration encodes password and assigns USER role")
    void testRegisterUserGreenSuccess() {
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(validUser.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        User saved = userService.registerUser(validUser);

        assertNotNull(saved.getId());
        assertEquals(Role.USER, saved.getRole());
        assertEquals("encodedPassword", saved.getPassword());
    }

    @Test
    @DisplayName("🟩 GREEN: Authenticate existing user with correct password")
    void testAuthenticateUserGreen() {
        validUser.setPassword("encodedPassword");
        when(userRepository.findByEmail(validUser.getEmail())).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches("Password123", "encodedPassword")).thenReturn(true);

        boolean result = userService.authenticateUser("test@example.com", "Password123");
        assertTrue(result);
    }

    @Test
    @DisplayName("🟩 GREEN: Validate password returns true for match")
    void testValidatePasswordMatch() {
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);

        boolean result = userService.validatePassword("raw", "encoded");
        assertTrue(result);
    }

    // ---------------- RED TESTS -------------------

    @Test
    @DisplayName("🟥 RED: Registration fails for weak password (<8 chars)")
    void testRegisterUserRedWeakPassword() {
        User weakUser = new User();
        weakUser.setEmail("weak@example.com");
        weakUser.setPassword("123"); // too short

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(weakUser));
    }

    @Test
    @DisplayName("🟥 RED: Registration fails for duplicate email")
    void testRegisterUserRedDuplicateEmail() {
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(validUser));
    }

    @Test
    @DisplayName("🟥 RED: Authentication fails for non-existent user")
    void testAuthenticateUserRedUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        boolean result = userService.authenticateUser("missing@example.com", "Password123");
        assertFalse(result);
    }

    @Test
    @DisplayName("🟥 RED: Validate password returns false for mismatch")
    void testValidatePasswordMismatch() {
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(false);

        boolean result = userService.validatePassword("raw", "encoded");
        assertFalse(result);
    }
}
