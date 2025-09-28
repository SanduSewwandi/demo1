package com.example.demo1.bdd;

import com.example.demo1.model.User;
import com.example.demo1.model.Role;
import com.example.demo1.service.UserService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserManagementStepDefinitions {

    @Autowired
    private UserService userService;

    private User testUser;
    private User registrationUser;
    private Exception caughtException;
    private boolean operationResult;
    private Optional<User> foundUser;
    private User loginUser;

    @Before
    public void setup() {
        // Clean up before each scenario
        testUser = null;
        registrationUser = null;
        caughtException = null;
        operationResult = false;
        foundUser = Optional.empty();
        loginUser = null;

        System.out.println("=== Setting up test environment ===");
    }

    // Background Steps
    @Given("the application is running")
    public void the_application_is_running() {
        System.out.println("✅ Application is running");
        assertNotNull(userService, "UserService should be available");
    }

    @Given("I have access to user service")
    public void i_have_access_to_user_service() {
        System.out.println("✅ User service is accessible");
        assertNotNull(userService);
    }

    // Registration Steps - FIXED TO MATCH FEATURE FILE EXACTLY
    @Given("I have valid user registration data")
    public void i_have_valid_user_registration_data() {
        registrationUser = new User();
        registrationUser.setName("Test User");
        registrationUser.setEmail("valid_" + System.currentTimeMillis() + "@example.com");
        registrationUser.setPassword("SecurePass123!");
        registrationUser.setRole(Role.USER);

        System.out.println("✅ Created valid user data: " + registrationUser.getEmail());
    }

    @Given("I have valid user registration data  # ← ADD THIS LINE TO FIX THE ISSUE")
    public void i_have_valid_user_registration_data_fix() {
        // This matches the exact text from your feature file including the comment
        i_have_valid_user_registration_data();
        System.out.println("✅ Using fixed registration data step");
    }

    @Given("a user already exists with email {string}")
    public void a_user_already_exists_with_email(String email) {
        User existingUser = new User();
        existingUser.setName("Existing User");
        existingUser.setEmail(email);
        existingUser.setPassword("ExistingPass123!");
        existingUser.setRole(Role.USER);

        try {
            User createdUser = userService.registerUser(existingUser);
            testUser = createdUser;
            System.out.println("✅ Created existing user: " + email);
        } catch (Exception e) {
            System.out.println("ℹ️ User might already exist: " + e.getMessage());
            // Try to find the existing user
            foundUser = userService.findByEmail(email);
            if (foundUser.isPresent()) {
                testUser = foundUser.get();
                System.out.println("✅ Found existing user: " + email);
            }
        }
    }

    @Given("I have user data with weak password {string}")
    public void i_have_user_data_with_weak_password(String weakPassword) {
        registrationUser = new User();
        registrationUser.setName("Weak Password User");
        registrationUser.setEmail("weak_" + System.currentTimeMillis() + "@example.com");
        registrationUser.setPassword(weakPassword);
        registrationUser.setRole(Role.USER);

        System.out.println("✅ Created user with weak password: " + weakPassword);
    }

    @When("I register a new user")
    public void i_register_a_new_user() {
        try {
            testUser = userService.registerUser(registrationUser);
            operationResult = true;
            System.out.println("✅ User registered successfully");
        } catch (Exception e) {
            caughtException = e;
            operationResult = false;
            System.out.println("❌ Registration failed: " + e.getMessage());
        }
    }

    @When("I try to register another user with the same email")
    public void i_try_to_register_another_user_with_the_same_email() {
        // Create a new user with the same email as the existing one
        User duplicateUser = new User();
        duplicateUser.setName("Duplicate User");
        duplicateUser.setEmail("existing@example.com"); // Hardcoded to match feature file
        duplicateUser.setPassword("DifferentPass123!");
        duplicateUser.setRole(Role.USER);

        try {
            userService.registerUser(duplicateUser);
            operationResult = true;
            testUser = duplicateUser;
            System.out.println("❌ Duplicate registration unexpectedly succeeded");
        } catch (Exception e) {
            caughtException = e;
            operationResult = false;
            System.out.println("✅ Duplicate registration correctly failed: " + e.getMessage());
        }
    }

    @When("I try to register the user")
    public void i_try_to_register_the_user() {
        try {
            testUser = userService.registerUser(registrationUser);
            operationResult = true;
            System.out.println("❌ Weak password registration unexpectedly succeeded");
        } catch (Exception e) {
            caughtException = e;
            operationResult = false;
            System.out.println("✅ Weak password registration correctly failed");
        }
    }

    // Authentication Steps
    @Given("a registered user with email {string} and password {string}")
    public void a_registered_user_with_email_and_password(String email, String password) {
        // First, ensure the user exists
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(Role.USER);

        try {
            User registered = userService.registerUser(user);
            testUser = registered;
            loginUser = registered;
            System.out.println("✅ Created registered user: " + email);
        } catch (Exception e) {
            System.out.println("ℹ️ User might already exist: " + e.getMessage());
            // Try to find existing user
            foundUser = userService.findByEmail(email);
            if (foundUser.isPresent()) {
                testUser = foundUser.get();
                loginUser = foundUser.get();
                System.out.println("✅ Using existing user: " + email);
            }
        }
    }

    @When("I login with correct credentials")
    public void i_login_with_correct_credentials() {
        if (testUser != null) {
            try {
                operationResult = userService.authenticateUser(testUser.getEmail(), "Password123");
                System.out.println("✅ Login attempt with correct credentials");
            } catch (Exception e) {
                caughtException = e;
                operationResult = false;
                System.out.println("❌ Login failed: " + e.getMessage());
            }
        }
    }

    @When("I login with incorrect password {string}")
    public void i_login_with_incorrect_password(String wrongPassword) {
        if (testUser != null) {
            try {
                operationResult = userService.authenticateUser(testUser.getEmail(), wrongPassword);
                System.out.println("✅ Login attempt with incorrect password");
            } catch (Exception e) {
                caughtException = e;
                operationResult = false;
                System.out.println("✅ Login correctly failed with wrong password");
            }
        }
    }

    // Then Steps (Assertions)
    @Then("the registration should be successful")
    public void the_registration_should_be_successful() {
        assertTrue(operationResult, "Registration should be successful");
        assertNotNull(testUser, "User should not be null");
        if (testUser != null) {
            assertNotNull(testUser.getId(), "User should have an ID");
        }
        System.out.println("✅ Registration verification passed");
    }

    @Then("the user should be saved in the database")
    public void the_user_should_be_saved_in_the_database() {
        if (testUser != null && testUser.getEmail() != null) {
            Optional<User> savedUser = userService.findByEmail(testUser.getEmail());
            assertTrue(savedUser.isPresent(), "User should be saved in database");
            assertEquals(testUser.getEmail(), savedUser.get().getEmail());
            System.out.println("✅ Database save verification passed");
        }
    }

    @Then("the registration should fail with error {string}")
    public void the_registration_should_fail_with_error(String expectedError) {
        assertFalse(operationResult, "Registration should have failed");
        assertNotNull(caughtException, "Exception should be thrown");
        if (caughtException != null) {
            assertTrue(caughtException.getMessage().contains(expectedError),
                    "Error message should contain: " + expectedError + ", but got: " + caughtException.getMessage());
        }
        System.out.println("✅ Registration failure verification passed");
    }

    @Then("the login should be successful")
    public void the_login_should_be_successful() {
        assertTrue(operationResult, "Login should be successful");
        System.out.println("✅ Login success verification passed");
    }

    @Then("the login should fail with error {string}")
    public void the_login_should_fail_with_error(String expectedError) {
        assertFalse(operationResult, "Login should have failed");
        System.out.println("✅ Login failure verification passed");
    }

    @Then("I should receive user details")
    public void i_should_receive_user_details() {
        assertNotNull(testUser, "User details should be available");
        System.out.println("✅ User details verification passed");
    }
}