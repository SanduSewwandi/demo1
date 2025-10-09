package com.example.demo1.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.File;
import java.time.Duration;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginUITest {

    private WebDriver driver;
    private WebDriverWait wait;
    private String uniqueUserDataDir;

    @BeforeEach
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        // Create unique user data directory to avoid conflicts
        String tempDir = System.getProperty("java.io.tmpdir");
        uniqueUserDataDir = tempDir + "chrome_profile_" + UUID.randomUUID().toString();

        // Chrome options for stability and to avoid conflicts
        options.addArguments("--user-data-dir=" + uniqueUserDataDir);
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--no-first-run");
        options.addArguments("--no-default-browser-check");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-plugins");

        // Alternative: Use incognito mode instead of user-data-dir
        // options.addArguments("--incognito");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        System.out.println("✅ WebDriver initialized with unique profile: " + uniqueUserDataDir);
    }

    @Test
    @Order(1)
    public void testInvalidCredentialsErrorMessage() {
        String frontendUrl = System.getenv("FRONTEND_URL");
        if (frontendUrl == null) {
            frontendUrl = "http://localhost:5173";
        }

        System.out.println("🚀 Testing Invalid Credentials Error Message");
        System.out.println("📝 Using frontend URL: " + frontendUrl);

        try {
            driver.get(frontendUrl + "/login");

            // Wait for page to load completely
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            // Debug info
            System.out.println("📄 Page title: " + driver.getTitle());
            System.out.println("📄 Current URL: " + driver.getCurrentUrl());

            // Find form elements using your data-testid attributes
            WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[data-testid='email-input']")
            ));

            WebElement passwordInput = driver.findElement(By.cssSelector(
                    "[data-testid='password-input']"
            ));

            WebElement loginButton = driver.findElement(By.cssSelector(
                    "[data-testid='login-button']"
            ));

            System.out.println("✅ Login form elements found");

            // Fill with specific test credentials that trigger "Invalid credentials" in your React code
            emailInput.clear();
            emailInput.sendKeys("test@example.com");

            passwordInput.clear();
            passwordInput.sendKeys("wrongpassword");

            System.out.println("✅ Filled invalid credentials: test@example.com / wrongpassword");

            // Click login button
            loginButton.click();
            System.out.println("✅ Clicked login button");

            // Wait for loading to complete (your React component has 1 second delay for invalid credentials)
            Thread.sleep(1500);

            // Wait for error message - your React component uses data-testid="error-message"
            WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[data-testid='error-message']")
            ));

            // Verify error message
            assertTrue(errorElement.isDisplayed(), "Error message should be visible");
            String errorText = errorElement.getText();
            System.out.println("📝 Error message text: " + errorText);

            // Check for "Invalid credentials" text (exact match from your React component)
            assertTrue(errorText.contains("Invalid credentials"),
                    "Error message should contain 'Invalid credentials'. Actual: " + errorText);

            // Verify password field is cleared (as per your React logic)
            String passwordValue = passwordInput.getAttribute("value");
            assertTrue(passwordValue == null || passwordValue.isEmpty(), "Password field should be cleared after error");

            System.out.println("✅ SUCCESS: Invalid credentials error displayed correctly!");

        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());

            // Enhanced debug info
            try {
                System.out.println("🔍 Debug Info:");
                System.out.println("Current URL: " + driver.getCurrentUrl());
                System.out.println("Page title: " + driver.getTitle());
                System.out.println("Page source length: " + driver.getPageSource().length());

                // Check if we can find any error elements
                java.util.List<WebElement> errorElements = driver.findElements(By.cssSelector(
                        "[data-testid='error-message'], .login-error-message, [class*='error'], .error, .text-danger"
                ));
                System.out.println("Found " + errorElements.size() + " potential error elements");

                for (WebElement elem : errorElements) {
                    System.out.println("Error element text: '" + elem.getText() + "' - Visible: " + elem.isDisplayed());
                }

                // Check if form elements exist
                java.util.List<WebElement> formElements = driver.findElements(By.cssSelector(
                        "input, button, form"
                ));
                System.out.println("Total form elements found: " + formElements.size());

            } catch (Exception debugEx) {
                System.err.println("Debug info failed: " + debugEx.getMessage());
            }

            // Take screenshot for debugging
            takeScreenshot("invalid_credentials_error");

            throw new RuntimeException("Test failed", e);
        }
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        String frontendUrl = System.getenv("FRONTEND_URL");
        if (frontendUrl == null) {
            frontendUrl = "http://localhost:5173";
        }

        System.out.println("🚀 Testing Successful Login");
        System.out.println("📝 Using frontend URL: " + frontendUrl);

        try {
            driver.get(frontendUrl + "/login");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            // Find form elements
            WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[data-testid='email-input']")
            ));

            WebElement passwordInput = driver.findElement(By.cssSelector(
                    "[data-testid='password-input']"
            ));

            WebElement loginButton = driver.findElement(By.cssSelector(
                    "[data-testid='login-button']"
            ));

            System.out.println("✅ Login form elements found");

            // Strategy 1: Try empty fields first (your React test login)
            emailInput.clear();
            passwordInput.clear();

            System.out.println("✅ Trying empty fields for test login...");
            loginButton.click();

            // Wait for the test login to process (your React has 800ms delay)
            Thread.sleep(1000);

            boolean success = false;
            String successMethod = "";

            // Check for success in multiple ways
            try {
                // Check for success message (your React component shows this)
                WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".login-success-message, [data-testid='success-message'], .success-message")
                ));
                if (successMessage.isDisplayed()) {
                    success = true;
                    successMethod = "success message";
                    String successText = successMessage.getText();
                    System.out.println("✅ SUCCESS: " + successText);
                }
            } catch (TimeoutException e1) {
                try {
                    // Check if redirected to home page (your React redirects after 1.5 seconds)
                    wait.until(ExpectedConditions.urlToBe(frontendUrl + "/"));
                    success = true;
                    successMethod = "redirect to home";
                    System.out.println("✅ SUCCESS: Redirected to home page!");
                } catch (TimeoutException e2) {
                    // Strategy 2: If empty fields don't work, try specific test credentials
                    System.out.println("🔄 Empty fields didn't work, trying test credentials...");

                    // Reload the page to reset state
                    driver.navigate().refresh();

                    // Wait for form again
                    emailInput = wait.until(ExpectedConditions.elementToBeClickable(
                            By.cssSelector("[data-testid='email-input']")
                    ));
                    passwordInput = driver.findElement(By.cssSelector(
                            "[data-testid='password-input']"
                    ));
                    loginButton = driver.findElement(By.cssSelector(
                            "[data-testid='login-button']"
                    ));

                    // Use the specific test credentials from your React component
                    emailInput.clear();
                    emailInput.sendKeys("test@greenscape.com");

                    passwordInput.clear();
                    passwordInput.sendKeys("test123");

                    System.out.println("✅ Using test credentials: test@greenscape.com / test123");
                    loginButton.click();

                    // Wait for success with longer timeout
                    try {
                        // Wait for success message
                        WebDriverWait successWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                        WebElement successMessage = successWait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector(".login-success-message, [data-testid='success-message'], .success-message")
                        ));
                        success = true;
                        successMethod = "success message with credentials";
                        System.out.println("✅ SUCCESS: Login success message displayed!");
                    } catch (TimeoutException e3) {
                        try {
                            // Wait for redirect to home page
                            WebDriverWait redirectWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                            redirectWait.until(ExpectedConditions.urlToBe(frontendUrl + "/"));
                            success = true;
                            successMethod = "redirect with credentials";
                            System.out.println("✅ SUCCESS: Redirected to home page!");
                        } catch (TimeoutException e4) {
                            // Check current URL to see what happened
                            String currentUrl = driver.getCurrentUrl();
                            System.out.println("❌ Login failed. Current URL: " + currentUrl);

                            // Check for any error messages
                            java.util.List<WebElement> errorElements = driver.findElements(By.cssSelector(
                                    "[data-testid='error-message'], .login-error-message, .error"
                            ));
                            if (!errorElements.isEmpty()) {
                                System.out.println("❌ Error message: " + errorElements.get(0).getText());
                            }
                        }
                    }
                }
            }

            // Final verification
            if (success) {
                System.out.println("✅ Login successful via: " + successMethod);

                // Wait a bit for localStorage to be updated
                Thread.sleep(1000);

                // Verify localStorage was set (your React component sets this)
                JavascriptExecutor js = (JavascriptExecutor) driver;
                Object isLoggedInObj = js.executeScript(
                        "return localStorage.getItem('isLoggedIn') === 'true';"
                );
                Boolean isLoggedIn = (Boolean) isLoggedInObj;
                assertTrue(isLoggedIn != null && isLoggedIn, "User should be logged in localStorage");

                // Verify token exists
                Object tokenObj = js.executeScript(
                        "return localStorage.getItem('token');"
                );
                String token = (String) tokenObj;
                assertNotNull(token, "Token should be set in localStorage");

                System.out.println("✅ SUCCESS: User logged in and localStorage updated!");
                System.out.println("📝 Token exists: " + (token != null && !token.isEmpty()));
                System.out.println("📝 isLoggedIn: " + isLoggedIn);
            }

            assertTrue(success, "Login should be successful. Method attempted: " + successMethod);

        } catch (Exception e) {
            System.err.println("❌ Successful login test failed: " + e.getMessage());

            // Enhanced debug info
            try {
                System.out.println("🔍 Debug Info:");
                System.out.println("Current URL: " + driver.getCurrentUrl());
                System.out.println("Page title: " + driver.getTitle());

                // Check for any error messages
                java.util.List<WebElement> errorElements = driver.findElements(By.cssSelector(
                        "[data-testid='error-message'], .login-error-message, .error"
                ));
                if (!errorElements.isEmpty()) {
                    System.out.println("Found error: " + errorElements.get(0).getText());
                }

                // Check localStorage state
                JavascriptExecutor js = (JavascriptExecutor) driver;
                Object isLoggedIn = js.executeScript("return localStorage.getItem('isLoggedIn');");
                Object token = js.executeScript("return localStorage.getItem('token');");
                System.out.println("LocalStorage - isLoggedIn: " + isLoggedIn + ", token: " + token);

            } catch (Exception debugEx) {
                System.err.println("Debug info failed: " + debugEx.getMessage());
            }

            // Take screenshot for debugging
            takeScreenshot("successful_login_error");

            throw new RuntimeException("Test failed", e);
        }
    }

    /**
     * Helper method to take screenshots for debugging
     */
    private void takeScreenshot(String testName) {
        try {
            if (driver instanceof TakesScreenshot) {
                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                // In a real scenario, you might want to save this to a specific directory
                System.out.println("📸 Screenshot taken for: " + testName);
            }
        } catch (Exception e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            try {
                // Clear localStorage before closing
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("localStorage.clear();");

                driver.quit();
                System.out.println("✅ WebDriver closed successfully");

                // Clean up temporary directory
                cleanUpTempDirectory();

            } catch (Exception e) {
                System.out.println("Error closing driver: " + e.getMessage());

                // Force cleanup if normal quit fails
                try {
                    driver.quit();
                } catch (Exception ex) {
                    System.out.println("Force quit also failed: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Clean up the temporary Chrome profile directory
     */
    private void cleanUpTempDirectory() {
        if (uniqueUserDataDir != null) {
            try {
                File tempDir = new File(uniqueUserDataDir);
                if (tempDir.exists()) {
                    deleteDirectory(tempDir);
                    System.out.println("✅ Temporary directory cleaned: " + uniqueUserDataDir);
                }
            } catch (Exception e) {
                System.out.println("⚠️ Could not clean temp directory: " + e.getMessage());
            }
        }
    }

    /**
     * Recursively delete a directory
     */
    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }
}