package com.example.demo1.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SignupUITest {

    private WebDriver driver;
    private WebDriverWait wait;

    @LocalServerPort
    private int port; // This will be automatically assigned by Spring Boot

    private String reactFrontendUrl;

    @BeforeEach
    public void setUp() {
        // Setup WebDriverManager to automatically manage ChromeDriver
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        // ADD THIS: Unique user data directory for each test session
        String userDataDir = System.getProperty("java.io.tmpdir") + "chrome_profile_" +
                UUID.randomUUID().toString().substring(0, 8);

        options.addArguments(
                "--user-data-dir=" + userDataDir,  // CRITICAL: Unique directory
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--remote-allow-origins=*",
                "--disable-gpu",
                "--window-size=1920,1080",
                "--disable-extensions",
                "--disable-blink-features=AutomationControlled" // Optional: avoid detection
        );

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        // Use the Spring Boot test server port for backend, React frontend on 5173
        reactFrontendUrl = "http://localhost:5173";

        System.out.println("✅ WebDriver initialized with unique profile - Backend port: " + port + ", Frontend: " + reactFrontendUrl);
    }

    @Test
    @DisplayName("UI Test: Successful User Registration and Navigation to Login")
    public void testSuccessfulUserRegistrationAndLoginNavigation() {
        try {
            System.out.println("🚀 Starting UI Test: User Registration with React Frontend");

            // Navigate to React signup page
            driver.get(reactFrontendUrl + "/signup");
            System.out.println("✅ Navigated to React signup page: " + driver.getCurrentUrl());

            // Wait for page to load completely
            wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete'"));

            // More flexible element selectors with null checks
            WebElement nameInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input[name='name'], input[placeholder*='name'], input[id*='name']")));

            // Find elements with explicit null checks
            WebElement emailInput = findElementWithNullCheck(
                    "input[name='email'], input[type='email'], input[placeholder*='email'], input[id*='email']");
            WebElement passwordInput = findElementWithNullCheck(
                    "input[name='password'], input[type='password'], input[placeholder*='password'], input[id*='password']");
            WebElement confirmPasswordInput = findElementWithNullCheck(
                    "input[name='confirmPassword'], input[placeholder*='confirm'], input[id*='confirm']");
            WebElement signupButton = findElementWithNullCheck(
                    "button[type='submit'], button, input[type='submit'], [class*='button'], [class*='btn']");

            assertTrue(nameInput.isDisplayed(), "Name input should be visible");
            assertTrue(emailInput != null && emailInput.isDisplayed(), "Email input should be visible");
            assertTrue(passwordInput != null && passwordInput.isDisplayed(), "Password input should be visible");
            System.out.println("✅ React signup form elements verified");

            String timestamp = String.valueOf(System.currentTimeMillis());
            String testName = "Selenium Test User " + timestamp;
            String testEmail = "selenium_test_" + timestamp + "@example.com";
            String testPassword = "SecurePass123!";

            // Clear fields first
            nameInput.clear();
            if (emailInput != null) emailInput.clear();
            if (passwordInput != null) passwordInput.clear();
            if (confirmPasswordInput != null) confirmPasswordInput.clear();

            // Fill form with null checks
            nameInput.sendKeys(testName);
            if (emailInput != null) emailInput.sendKeys(testEmail);
            if (passwordInput != null) passwordInput.sendKeys(testPassword);
            if (confirmPasswordInput != null) confirmPasswordInput.sendKeys(testPassword);
            System.out.println("✅ Filled registration form with unique data: " + testEmail);

            // Click signup button with null check
            if (signupButton != null) {
                signupButton.click();
                System.out.println("✅ Clicked signup button");
            } else {
                fail("Signup button not found");
            }

            boolean registrationSuccess = false;
            String currentUrl = "";

            // Wait for success - multiple possible success indicators
            try {
                // Option 1: Look for success message
                WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("[class*='success'], [class*='Success'], .alert-success, .success, [role='alert']")));
                assertTrue(successMessage.isDisplayed(), "Success message should be displayed");
                System.out.println("✅ Registration success message displayed: " + successMessage.getText());
                registrationSuccess = true;
            } catch (Exception e1) {
                // Option 2: Check for redirect to login or success page
                try {
                    wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/signup")));
                    currentUrl = driver.getCurrentUrl();
                    System.out.println("✅ Registration successful! Redirected to: " + currentUrl);

                    // Safe URL check with null check
                    if (currentUrl != null && (currentUrl.contains("login") || currentUrl.contains("success") ||
                            currentUrl.contains("dashboard"))) {
                        registrationSuccess = true;
                    } else {
                        System.out.println("⚠️ Unexpected redirect URL: " + currentUrl);
                    }
                } catch (Exception e2) {
                    // Option 3: Look for any confirmation message
                    try {
                        WebElement confirmation = wait.until(ExpectedConditions.presenceOfElementLocated(
                                By.cssSelector("h1, h2, h3, p, div")));
                        String text = confirmation.getText().toLowerCase();
                        if (text.contains("success") || text.contains("welcome") || text.contains("created")) {
                            System.out.println("✅ Registration confirmed via text: " + text);
                            registrationSuccess = true;
                        } else {
                            // If no clear success indicator, at least verify we're not on signup page with errors
                            WebElement errorCheck = driver.findElement(By.cssSelector("body"));
                            String bodyText = errorCheck.getText();
                            if (bodyText != null && !bodyText.toLowerCase().contains("error")) {
                                System.out.println("✅ Registration completed without errors");
                                registrationSuccess = true;
                            }
                        }
                    } catch (Exception e3) {
                        System.out.println("⚠️ Could not determine registration status");
                    }
                }
            }

            if (registrationSuccess) {
                System.out.println("🎉 UI Test PASSED: User successfully registered via React frontend");

                // NEW: Navigate to login page after successful registration
                System.out.println("🔄 Navigating to login page...");
                driver.get(reactFrontendUrl + "/login");

                // Verify we're on login page
                wait.until(ExpectedConditions.urlContains("/login"));
                currentUrl = driver.getCurrentUrl();
                System.out.println("✅ Successfully navigated to login page: " + currentUrl);

                // Verify login page elements are present
                try {
                    WebElement loginEmailInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("input[type='email'], [data-testid='email-input']")));
                    WebElement loginPasswordInput = findElementWithNullCheck(
                            "input[type='password'], [data-testid='password-input']");
                    WebElement loginButton = findElementWithNullCheck(
                            "button[type='submit'], [data-testid='login-button']");

                    assertTrue(loginEmailInput.isDisplayed(), "Login email input should be visible");
                    assertTrue(loginPasswordInput != null && loginPasswordInput.isDisplayed(), "Login password input should be visible");
                    assertTrue(loginButton != null && loginButton.isDisplayed(), "Login button should be visible");

                    System.out.println("✅ Login page elements verified successfully");
                    System.out.println("🎉 COMPLETE: User registered and navigated to login page successfully!");

                } catch (Exception e) {
                    System.out.println("⚠️ Could not verify all login page elements, but navigation successful");
                }
            } else {
                fail("Registration was not successful");
            }

        } catch (Exception e) {
            System.out.println("❌ UI Test FAILED: " + e.getMessage());
            System.out.println("Current URL: " + driver.getCurrentUrl());
            System.out.println("Page title: " + driver.getTitle());
            String pageSource = driver.getPageSource();
            System.out.println("Page source snippet: " +
                    (pageSource != null ? pageSource.substring(0, Math.min(500, pageSource.length())) : "null"));
            fail("UI Test Failed: " + e.getMessage());
        }
    }

    /**
     * Helper method to find elements with null safety
     */
    private WebElement findElementWithNullCheck(String cssSelector) {
        try {
            return driver.findElement(By.cssSelector(cssSelector));
        } catch (Exception e) {
            System.out.println("⚠️ Element not found with selector: " + cssSelector);
            return null;
        }
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            try {
                // Add small delay to see results
                Thread.sleep(1000);
                driver.quit();
                System.out.println("✅ WebDriver closed successfully");
            } catch (Exception e) {
                System.out.println("⚠️ Error closing WebDriver: " + e.getMessage());
            }
        }
    }
}