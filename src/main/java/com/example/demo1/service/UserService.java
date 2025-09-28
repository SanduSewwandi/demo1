package com.example.demo1.service;

import com.example.demo1.model.Role;
import com.example.demo1.model.User;
import com.example.demo1.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final int MIN_PASSWORD_LENGTH = 8;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register a user
     * ✅ Throws IllegalArgumentException for weak password or duplicate email
     * ✅ Encodes password and assigns USER role
     */
    public User registerUser(User user) {
        logger.info("Attempting to register user with email: {}", user.getEmail());

        validateUserRegistration(user);

        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("Registration failed - duplicate email: {}", user.getEmail());
            throw new IllegalArgumentException("User already exists with email: " + user.getEmail());
        }

        if (!isPasswordStrong(user.getPassword())) {
            logger.warn("Registration failed - weak password for email: {}", user.getEmail());
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }

        // Prepare user for registration
        User userToSave = prepareUserForRegistration(user);

        User savedUser = userRepository.save(userToSave);
        logger.info("User registered successfully with ID: {}", savedUser.getId());

        return savedUser;
    }

    /**
     * Authenticate user
     */
    public boolean authenticateUser(String email, String rawPassword) {
        logger.debug("Authenticating user with email: {}", email);

        if (!StringUtils.hasText(email) || !StringUtils.hasText(rawPassword)) {
            logger.warn("Authentication failed - empty email or password");
            return false;
        }

        Optional<User> userOptional = userRepository.findByEmail(email.trim());

        if (userOptional.isEmpty()) {
            logger.warn("Authentication failed - user not found: {}", email);
            return false;
        }

        User user = userOptional.get();
        boolean isAuthenticated = passwordEncoder.matches(rawPassword, user.getPassword());

        if (isAuthenticated) {
            logger.info("User authenticated successfully: {}", email);
        } else {
            logger.warn("Authentication failed - invalid password for: {}", email);
        }

        return isAuthenticated;
    }

    /**
     * Validate password
     */
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(encodedPassword)) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Find user by email with validation
     */
    public Optional<User> findByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.trim());
    }

    /**
     * Get all users with logging
     */
    public List<User> getAllUsers() {
        logger.debug("Fetching all users");
        List<User> users = userRepository.findAll();
        logger.info("Retrieved {} users from database", users.size());
        return users;
    }

    /**
     * Get user by ID with validation
     */
    public Optional<User> getUserById(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return userRepository.findById(id);
    }

    /**
     * Delete user with validation and logging
     */
    public boolean deleteUser(Long id) {
        if (id == null || id <= 0) {
            logger.warn("Delete user failed - invalid ID: {}", id);
            return false;
        }

        if (!userRepository.existsById(id)) {
            logger.warn("Delete user failed - user not found with ID: {}", id);
            return false;
        }

        try {
            userRepository.deleteById(id);
            logger.info("User deleted successfully with ID: {}", id);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting user with ID: {}", id, e);
            return false;
        }
    }

    // ============ NEW REFACTORED METHODS ============

    /**
     * 🆕 Check if user exists by email
     */
    public boolean userExists(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return userRepository.existsByEmail(email.trim());
    }

    /**
     * 🆕 Validate password strength
     */
    public boolean isPasswordStrong(String password) {
        if (!StringUtils.hasText(password)) {
            return false;
        }
        return password.length() >= MIN_PASSWORD_LENGTH;
    }

    /**
     * 🆕 Update user profile
     */
    public Optional<User> updateUser(Long id, User userUpdates) {
        if (id == null || id <= 0 || userUpdates == null) {
            return Optional.empty();
        }

        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isEmpty()) {
            logger.warn("Update failed - user not found with ID: {}", id);
            return Optional.empty();
        }

        User user = existingUser.get();
        updateUserFields(user, userUpdates);

        User updatedUser = userRepository.save(user);
        logger.info("User updated successfully with ID: {}", id);

        return Optional.of(updatedUser);
    }

    /**
     * 🆕 Change user role (admin function)
     */
    public Optional<User> changeUserRole(Long id, Role newRole) {
        if (id == null || id <= 0 || newRole == null) {
            return Optional.empty();
        }

        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();
        user.setRole(newRole);

        User updatedUser = userRepository.save(user);
        logger.info("User role changed to {} for ID: {}", newRole, id);

        return Optional.of(updatedUser);
    }

    // ============ PRIVATE HELPER METHODS ============

    /**
     * Validate user registration data
     */
    private void validateUserRegistration(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (!StringUtils.hasText(user.getName())) {
            throw new IllegalArgumentException("User name is required");
        }

        if (!StringUtils.hasText(user.getEmail())) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (!StringUtils.hasText(user.getPassword())) {
            throw new IllegalArgumentException("Password is required");
        }
    }

    /**
     * Prepare user for registration
     */
    private User prepareUserForRegistration(User user) {
        User userToSave = new User();
        userToSave.setName(user.getName().trim());
        userToSave.setEmail(user.getEmail().trim().toLowerCase());
        userToSave.setPassword(passwordEncoder.encode(user.getPassword()));
        userToSave.setRole(user.getRole() != null ? user.getRole() : Role.USER);
        return userToSave;
    }

    /**
     * Update user fields selectively
     */
    private void updateUserFields(User existingUser, User updates) {
        if (StringUtils.hasText(updates.getName())) {
            existingUser.setName(updates.getName().trim());
        }

        if (StringUtils.hasText(updates.getEmail()) && isValidEmail(updates.getEmail())) {
            // Check if new email is not already taken by another user
            if (!existingUser.getEmail().equalsIgnoreCase(updates.getEmail().trim()) &&
                    userRepository.existsByEmail(updates.getEmail().trim())) {
                throw new IllegalArgumentException("Email already taken: " + updates.getEmail());
            }
            existingUser.setEmail(updates.getEmail().trim().toLowerCase());
        }

        if (StringUtils.hasText(updates.getPassword())) {
            if (!isPasswordStrong(updates.getPassword())) {
                throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
            }
            existingUser.setPassword(passwordEncoder.encode(updates.getPassword()));
        }

        if (updates.getRole() != null) {
            existingUser.setRole(updates.getRole());
        }
    }

    /**
     * Basic email validation
     */
    private boolean isValidEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    /**
     * 🆕 Get user count
     */
    public long getUserCount() {
        return userRepository.count();
    }

    /**
     * 🆕 Check if user is admin
     */
    public boolean isAdmin(Long userId) {
        if (userId == null || userId <= 0) {
            return false;
        }
        return userRepository.findById(userId)
                .map(user -> user.getRole() == Role.ADMIN)
                .orElse(false);
    }
}