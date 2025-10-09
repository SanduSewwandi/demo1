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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        try {
            System.out.println("🚀 Setting up WebDriver...");

            // Kill any remaining Chrome processes
            killChromeProcesses();

            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();

            // Use incognito mode instead of user-data-dir to avoid conflicts
            options.addArguments("--incognito");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-gpu");
            // options.addArguments("--headless"); // Uncomment for CI environments

            driver = new ChromeDriver(options);
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
            wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            System.out.println("✅ WebDriver initialized successfully");

        } catch (Exception e) {
            System.err.println("❌ Failed to setup WebDriver: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("WebDriver setup failed", e);
        }
    }

    @Test
    @Order(1)
    public void testInvalidCredentialsErrorMessage() {
        String frontendUrl = "http://localhost:5173"; // Use direct URL for testing
        System.out.println("🚀 Testing Invalid Credentials Error Message");
        System.out.println("📝 Using frontend URL: " + frontendUrl);

        try {
            // Navigate to login page
            String loginUrl = frontendUrl + "/login";
            System.out.println("🌐 Navigating to: " + loginUrl);
            driver.get(loginUrl);

            // Wait for page to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            System.out.println("✅ Page loaded successfully");

            // Print basic page info
            System.out.println("📄 Page title: " + driver.getTitle());
            System.out.println("📄 Current URL: " + driver.getCurrentUrl());

            // Wait a bit for React to load
            Thread.sleep(3000);

            // Debug: Print page source to see what's actually there
            printPageDebugInfo();

            // Try to find form elements with multiple strategies
            WebElement emailInput = findElementMultipleStrategies("email");
            WebElement passwordInput = findElementMultipleStrategies("password");
            WebElement loginButton = findElementMultipleStrategies("login-button");

            System.out.println("✅ All login form elements found");

            // Fill with invalid credentials
            System.out.println("📝 Filling invalid credentials...");
            emailInput.clear();
            emailInput.sendKeys("test@example.com");

            passwordInput.clear();
            passwordInput.sendKeys("wrongpassword");

            // Click login button
            System.out.println("🖱️ Clicking login button...");
            loginButton.click();

            // Wait for response
            System.out.println("⏳ Waiting for response...");
            Thread.sleep(3000); // Wait longer for the response

            // Look for error message with multiple selectors
            WebElement errorElement = findErrorElement();

            // Verify error message
            assertTrue(errorElement.isDisplayed(), "Error message should be visible");
            String errorText = errorElement.getText();
            System.out.println("📝 Error message text: '" + errorText + "'");

            assertTrue(errorText.toLowerCase().contains("invalid credentials"),
                    "Error message should contain 'Invalid credentials'. Actual: " + errorText);

            System.out.println("✅ SUCCESS: Invalid credentials test passed!");

        } catch (Exception e) {
            System.err.println("❌ Test failed with exception: " + e.getMessage());
            e.printStackTrace();
            takeScreenshot("invalid_credentials_failure");
            throw new RuntimeException("Invalid credentials test failed", e);
        }
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        String frontendUrl = "http://localhost:5173";
        System.out.println("🚀 Testing Successful Login");
        System.out.println("📝 Using frontend URL: " + frontendUrl);

        try {
            driver.get(frontendUrl + "/login");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            System.out.println("✅ Login page loaded");

            // Wait for React
            Thread.sleep(3000);

            // Find form elements
            WebElement emailInput = findElementMultipleStrategies("email");
            WebElement passwordInput = findElementMultipleStrategies("password");
            WebElement loginButton = findElementMultipleStrategies("login-button");

            System.out.println("✅ Login form elements found");

            // Use test credentials
            System.out.println("📝 Using test credentials...");
            emailInput.clear();
            emailInput.sendKeys("test@greenscape.com");

            passwordInput.clear();
            passwordInput.sendKeys("test123");

            // Click login button
            loginButton.click();
            System.out.println("🖱️ Clicked login button");

            // Wait for success
            Thread.sleep(3000);

            // Check for success
            boolean success = checkLoginSuccess(frontendUrl);

            if (success) {
                System.out.println("✅ SUCCESS: Login successful!");

                // Verify localStorage
                verifyLocalStorage();

            } else {
                System.out.println("❌ Login failed");
                printPageDebugInfo();
                takeScreenshot("login_failure");
                fail("Login was not successful");
            }

        } catch (Exception e) {
            System.err.println("❌ Successful login test failed: " + e.getMessage());
            e.printStackTrace();
            takeScreenshot("successful_login_exception");
            throw new RuntimeException("Successful login test failed", e);
        }
    }

    /**
     * Try multiple strategies to find elements
     */
    private WebElement findElementMultipleStrategies(String elementType) {
        String[] selectors;

        switch (elementType) {
            case "email":
                selectors = new String[]{
                        "[data-testid='email-input']",
                        "input[type='email']",
                        "input[name='email']",
                        "input[placeholder*='email' i]",
                        "#email",
                        ".email-input"
                };
                break;
            case "password":
                selectors = new String[]{
                        "[data-testid='password-input']",
                        "input[type='password']",
                        "input[name='password']",
                        "input[placeholder*='password' i]",
                        "#password",
                        ".password-input"
                };
                break;
            case "login-button":
                selectors = new String[]{
                        "[data-testid='login-button']",
                        "button[type='submit']",
                        "button:contains('Login')",
                        "input[type='submit']",
                        "#login-btn",
                        ".login-button"
                };
                break;
            default:
                throw new IllegalArgumentException("Unknown element type: " + elementType);
        }

        for (String selector : selectors) {
            try {
                System.out.println("🔍 Trying selector: " + selector);
                WebElement element = driver.findElement(By.cssSelector(selector));
                if (element.isDisplayed() && element.isEnabled()) {
                    System.out.println("✅ Found element with: " + selector);
                    return element;
                }
            } catch (Exception e) {
                // Continue to next selector
            }
        }

        // If nothing found, print available elements for debugging
        printAllInputsAndButtons();
        throw new RuntimeException("Could not find " + elementType + " element with any selector");
    }

    /**
     * Find error element with multiple strategies
     */
    private WebElement findErrorElement() {
        String[] errorSelectors = {
                "[data-testid='error-message']",
                ".error-message",
                ".text-danger",
                ".alert-danger",
                "[class*='error']",
                "[role='alert']",
                ".MuiAlert-root",
                ".error"
        };

        for (String selector : errorSelectors) {
            try {
                WebElement element = driver.findElement(By.cssSelector(selector));
                if (element.isDisplayed()) {
                    System.out.println("✅ Found error element with: " + selector);
                    return element;
                }
            } catch (Exception e) {
                // Continue to next selector
            }
        }

        throw new RuntimeException("Could not find error element with any selector");
    }

    /**
     * Check if login was successful
     */
    private boolean checkLoginSuccess(String frontendUrl) {
        // Check for redirect
        String currentUrl = driver.getCurrentUrl();
        if (!currentUrl.contains("/login")) {
            System.out.println("✅ Success: Redirected to: " + currentUrl);
            return true;
        }

        // Check for success message
        String[] successSelectors = {
                ".login-success-message",
                "[data-testid='success-message']",
                ".alert-success",
                "[class*='success']"
        };

        for (String selector : successSelectors) {
            try {
                WebElement element = driver.findElement(By.cssSelector(selector));
                if (element.isDisplayed()) {
                    System.out.println("✅ Success: Found success message: " + element.getText());
                    return true;
                }
            } catch (Exception e) {
                // Continue
            }
        }

        return false;
    }

    /**
     * Verify localStorage after login
     */
    private void verifyLocalStorage() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Check isLoggedIn
            Object isLoggedIn = js.executeScript("return localStorage.getItem('isLoggedIn');");
            System.out.println("📝 localStorage isLoggedIn: " + isLoggedIn);

            // Check token
            Object token = js.executeScript("return localStorage.getItem('token');");
            System.out.println("📝 localStorage token: " + token);

            if ("true".equals(isLoggedIn) && token != null && !"null".equals(token)) {
                System.out.println("✅ localStorage verification passed");
            } else {
                System.out.println("⚠️ localStorage values not as expected");
            }

        } catch (Exception e) {
            System.out.println("⚠️ Could not verify localStorage: " + e.getMessage());
        }
    }

    /**
     * Print debug information about the page
     */
    private void printPageDebugInfo() {
        try {
            System.out.println("\n🔍 === PAGE DEBUG INFO ===");
            System.out.println("Current URL: " + driver.getCurrentUrl());
            System.out.println("Page title: " + driver.getTitle());

            // Print all data-testid elements
            java.util.List<WebElement> testIdElements = driver.findElements(By.cssSelector("[data-testid]"));
            System.out.println("Elements with data-testid (" + testIdElements.size() + "):");
            for (WebElement elem : testIdElements) {
                String testId = elem.getAttribute("data-testid");
                System.out.println("  - data-testid: '" + testId + "', tag: " + elem.getTagName() +
                        ", text: '" + elem.getText() + "', visible: " + elem.isDisplayed());
            }

            System.out.println("=== END DEBUG INFO ===\n");

        } catch (Exception e) {
            System.err.println("Could not print debug info: " + e.getMessage());
        }
    }

    /**
     * Print all inputs and buttons for debugging
     */
    private void printAllInputsAndButtons() {
        try {
            System.out.println("\n🔍 === ALL INPUTS AND BUTTONS ===");

            // Print all inputs
            java.util.List<WebElement> inputs = driver.findElements(By.tagName("input"));
            System.out.println("Inputs (" + inputs.size() + "):");
            for (WebElement input : inputs) {
                String type = input.getAttribute("type");
                String name = input.getAttribute("name");
                String placeholder = input.getAttribute("placeholder");
                String testid = input.getAttribute("data-testid");
                System.out.println("  - type: " + type + ", name: " + name +
                        ", placeholder: " + placeholder + ", data-testid: " + testid +
                        ", visible: " + input.isDisplayed());
            }

            // Print all buttons
            java.util.List<WebElement> buttons = driver.findElements(By.tagName("button"));
            System.out.println("Buttons (" + buttons.size() + "):");
            for (WebElement button : buttons) {
                String text = button.getText();
                String testid = button.getAttribute("data-testid");
                System.out.println("  - text: '" + text + "', data-testid: " + testid +
                        ", visible: " + button.isDisplayed());
            }

            System.out.println("=== END INPUTS/BUTTONS ===\n");

        } catch (Exception e) {
            System.err.println("Could not print inputs/buttons: " + e.getMessage());
        }
    }

    /**
     * Kill Chrome processes
     */
    private void killChromeProcesses() {
        try {
            Runtime.getRuntime().exec("taskkill /F /IM chrome.exe");
            Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe");
            Thread.sleep(2000); // Wait for processes to terminate
            System.out.println("✅ Killed Chrome processes");
        } catch (Exception e) {
            System.out.println("⚠️ Could not kill Chrome processes: " + e.getMessage());
        }
    }

    /**
     * Take screenshot
     */
    private void takeScreenshot(String testName) {
        try {
            if (driver instanceof TakesScreenshot) {
                // Create screenshots directory
                Path screenshotsDir = Paths.get("screenshots");
                if (!Files.exists(screenshotsDir)) {
                    Files.createDirectories(screenshotsDir);
                }

                // Take screenshot
                File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                String timestamp = String.valueOf(System.currentTimeMillis());
                String filename = "screenshots/" + testName + "_" + timestamp + ".png";

                Files.copy(screenshotFile.toPath(), Paths.get(filename));
                System.out.println("📸 Screenshot saved: " + filename);
            }
        } catch (Exception e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            try {
                System.out.println("🧹 Cleaning up...");
                driver.quit();
                System.out.println("✅ WebDriver closed successfully");
            } catch (Exception e) {
                System.out.println("❌ Error during cleanup: " + e.getMessage());
            }
        }
    }
}