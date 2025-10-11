package com.example.demo1.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.Duration;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginUITest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        // Set ChromeDriver path explicitly for CI environment
        String chromeDriverPath = System.getenv("webdriver.chrome.driver");
        if (chromeDriverPath != null) {
            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
            System.out.println("✅ Using ChromeDriver from environment: " + chromeDriverPath);
        } else {
            // Fallback: Use WebDriverManager only if not in CI
            try {
                Class.forName("io.github.bonigarcia.wdm.WebDriverManager");
                io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
                System.out.println("✅ Using WebDriverManager for ChromeDriver setup");
            } catch (ClassNotFoundException e) {
                System.out.println("⚠️ WebDriverManager not available, relying on system ChromeDriver");
            }
        }

        ChromeOptions options = new ChromeOptions();

        // Headless mode for CI environment
        boolean isCI = System.getenv("CI") != null;
        if (isCI) {
            options.addArguments("--headless");
            System.out.println("✅ Running in HEADLESS mode (CI environment)");
        }

        // Unique user data directory for each test session
        String userDataDir = System.getProperty("java.io.tmpdir") + "chrome_profile_" +
                UUID.randomUUID().toString().substring(0, 8);

        options.addArguments(
                "--user-data-dir=" + userDataDir,
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--window-size=1920,1080",
                "--remote-allow-origins=*",
                "--disable-blink-features=AutomationControlled",
                "--disable-extensions",
                "--disable-gpu",
                "--disable-software-rasterizer",
                "--disable-web-security",
                "--allow-running-insecure-content",
                "--ignore-certificate-errors"
        );

        try {
            driver = new ChromeDriver(options);
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            System.out.println("✅ WebDriver initialized successfully");
            System.out.println("✅ Profile directory: " + userDataDir);
            System.out.println("✅ CI Environment: " + isCI);

        } catch (Exception e) {
            System.err.println("❌ Failed to initialize WebDriver: " + e.getMessage());
            throw new RuntimeException("WebDriver initialization failed", e);
        }
    }

    @Test
    public void testInvalidCredentialsErrorMessage() {
        String frontendUrl = System.getenv("FRONTEND_URL");
        if (frontendUrl == null) {
            frontendUrl = "http://localhost:5173";
        }

        System.out.println("🚀 Testing Invalid Credentials Error Message");
        System.out.println("🌐 Frontend URL: " + frontendUrl);

        try {
            driver.get(frontendUrl + "/login");

            // Wait for page to load completely
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            // Additional wait for React to load
            Thread.sleep(2000);

            // Debug info
            System.out.println("📄 Page title: " + driver.getTitle());
            System.out.println("📄 Current URL: " + driver.getCurrentUrl());
            System.out.println("📄 Page source available: " + (driver.getPageSource().length() > 0));

            // Try multiple selectors for form elements
            WebElement emailInput = findElementWithMultipleSelectors(
                    "[data-testid='email-input']",
                    "input[type='email']",
                    "input[name='email']",
                    "#email",
                    "input[placeholder*='email' i]",
                    "input[placeholder*='Email' i]"
            );

            WebElement passwordInput = findElementWithMultipleSelectors(
                    "[data-testid='password-input']",
                    "input[type='password']",
                    "input[name='password']",
                    "#password",
                    "input[placeholder*='password' i]",
                    "input[placeholder*='Password' i]"
            );

            WebElement loginButton = findElementWithMultipleSelectors(
                    "[data-testid='login-button']",
                    "button[type='submit']",
                    "button:contains('Login')",
                    ".login-button",
                    "input[type='submit']",
                    "button[type='button']"
            );

            System.out.println("✅ Login form elements found");

            // Fill with invalid credentials
            emailInput.clear();
            emailInput.sendKeys("test@example.com");

            passwordInput.clear();
            passwordInput.sendKeys("wrongpassword");

            System.out.println("✅ Filled invalid credentials: test@example.com / wrongpassword");

            // Click login button
            loginButton.click();
            System.out.println("✅ Clicked login button");

            // Wait for response - increased timeout for CI
            Thread.sleep(3000);

            // Wait for error message with multiple possible selectors
            WebElement errorElement = findElementWithTimeout(
                    "[data-testid='error-message']",
                    ".error-message",
                    "[class*='error']",
                    ".alert-danger",
                    ".text-danger",
                    "[role='alert']",
                    ".MuiAlert-root"  // Material-UI alert
            );

            // Verify error message
            assertTrue(errorElement.isDisplayed(), "Error message should be visible");
            String errorText = errorElement.getText();
            System.out.println("📝 Error message text: " + errorText);

            // Flexible error message checking
            boolean hasErrorText = errorText.toLowerCase().contains("invalid") ||
                    errorText.toLowerCase().contains("error") ||
                    errorText.toLowerCase().contains("incorrect") ||
                    errorText.toLowerCase().contains("wrong") ||
                    errorText.toLowerCase().contains("fail");

            assertTrue(hasErrorText,
                    "Error message should indicate failure. Actual: " + errorText);

            System.out.println("✅ SUCCESS: Invalid credentials error displayed correctly!");

        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            captureDebugInfo();
            throw new RuntimeException("Test failed", e);
        }
    }

    @Test
    public void testSuccessfulLogin() {
        String frontendUrl = System.getenv("FRONTEND_URL");
        if (frontendUrl == null) {
            frontendUrl = "http://localhost:5173";
        }

        System.out.println("🚀 Testing Successful Login");
        System.out.println("🌐 Frontend URL: " + frontendUrl);

        try {
            driver.get(frontendUrl + "/login");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            // Additional wait for React to load
            Thread.sleep(2000);

            // Find form elements with multiple selectors
            WebElement emailInput = findElementWithMultipleSelectors(
                    "[data-testid='email-input']",
                    "input[type='email']",
                    "input[name='email']",
                    "#email",
                    "input[placeholder*='email' i]",
                    "input[placeholder*='Email' i]"
            );

            WebElement passwordInput = findElementWithMultipleSelectors(
                    "[data-testid='password-input']",
                    "input[type='password']",
                    "input[name='password']",
                    "#password",
                    "input[placeholder*='password' i]",
                    "input[placeholder*='Password' i]"
            );

            WebElement loginButton = findElementWithMultipleSelectors(
                    "[data-testid='login-button']",
                    "button[type='submit']",
                    "button:contains('Login')",
                    ".login-button",
                    "input[type='submit']"
            );

            boolean success = false;

            // Try multiple credential strategies
            String[][] testCredentials = {
                    {"test@greenscape.com", "test123"},
                    {"admin@example.com", "admin123"},
                    {"user@example.com", "password123"},
                    {"demo@demo.com", "demo123"}
            };

            for (String[] credentials : testCredentials) {
                System.out.println("🔄 Trying credentials: " + credentials[0] + " / " + credentials[1]);

                emailInput.clear();
                emailInput.sendKeys(credentials[0]);

                passwordInput.clear();
                passwordInput.sendKeys(credentials[1]);

                loginButton.click();

                // Wait for response
                Thread.sleep(3000);

                // Check for success indicators
                if (isLoginSuccessful(frontendUrl)) {
                    success = true;
                    System.out.println("✅ SUCCESS: Logged in with " + credentials[0]);
                    break;
                } else {
                    System.out.println("❌ Failed with " + credentials[0] + ", trying next...");
                    // Reload page for next attempt
                    driver.get(frontendUrl + "/login");
                    Thread.sleep(1000);

                    // Re-find elements
                    emailInput = findElementWithMultipleSelectors(
                            "[data-testid='email-input']",
                            "input[type='email']",
                            "input[name='email']"
                    );
                    passwordInput = findElementWithMultipleSelectors(
                            "[data-testid='password-input']",
                            "input[type='password']",
                            "input[name='password']"
                    );
                    loginButton = findElementWithMultipleSelectors(
                            "[data-testid='login-button']",
                            "button[type='submit']"
                    );
                }
            }

            if (success) {
                // Verify localStorage or session indicators
                JavascriptExecutor js = (JavascriptExecutor) driver;

                // Check multiple possible success indicators
                Object isLoggedIn = js.executeScript(
                        "return localStorage.getItem('isLoggedIn') === 'true' || " +
                                "sessionStorage.getItem('isLoggedIn') === 'true' || " +
                                "localStorage.getItem('token') !== null || " +
                                "sessionStorage.getItem('token') !== null || " +
                                "document.cookie.includes('auth') || " +
                                "document.cookie.includes('token');"
                );

                if (Boolean.TRUE.equals(isLoggedIn)) {
                    System.out.println("✅ SUCCESS: Authentication confirmed in storage");
                } else {
                    System.out.println("⚠️ Login apparent but no storage evidence found");
                }

                // Check if we're on a protected/dashboard page
                String currentUrl = driver.getCurrentUrl();
                if (!currentUrl.contains("/login")) {
                    System.out.println("✅ SUCCESS: Navigated away from login page to: " + currentUrl);
                }
            }

            assertTrue(success, "Login should be successful with at least one set of credentials");

        } catch (Exception e) {
            System.err.println("❌ Successful login test failed: " + e.getMessage());
            captureDebugInfo();
            throw new RuntimeException("Test failed", e);
        }
    }

    // Helper method to check for successful login
    private boolean isLoginSuccessful(String frontendUrl) {
        try {
            // Check if URL changed from login page
            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.contains("/login") && !currentUrl.equals(frontendUrl + "/login")) {
                return true;
            }

            // Check for success messages
            try {
                WebElement successElement = new WebDriverWait(driver, Duration.ofSeconds(3))
                        .until(ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector(".success, .login-success, [data-testid='success-message']")
                        ));
                return successElement.isDisplayed();
            } catch (TimeoutException e) {
                // Ignore - no success message found
            }

            // Check for dashboard elements
            String[] dashboardIndicators = {
                    "[data-testid='dashboard']",
                    "[data-testid='welcome']",
                    ".dashboard",
                    ".welcome",
                    "h1:contains('Welcome')",
                    "h1:contains('Dashboard')"
            };

            for (String selector : dashboardIndicators) {
                try {
                    if (driver.findElement(By.cssSelector(selector)).isDisplayed()) {
                        return true;
                    }
                } catch (NoSuchElementException e) {
                    // Continue to next selector
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // Helper method to find elements with multiple selector strategies
    private WebElement findElementWithMultipleSelectors(String... selectors) {
        for (String selector : selectors) {
            try {
                // Try CSS selector first
                WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(selector)));
                System.out.println("✅ Found element with selector: " + selector);
                return element;
            } catch (TimeoutException e) {
                System.out.println("❌ Selector not found: " + selector);
            }
        }

        // Last resort: try to find any input or button
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("input, button, [role='button']")
            ));
            System.out.println("✅ Found fallback element: " + element.getTagName());
            return element;
        } catch (TimeoutException e) {
            throw new NoSuchElementException("None of the selectors worked: " + String.join(", ", selectors));
        }
    }

    // Helper method to find elements with shorter timeout for error messages
    private WebElement findElementWithTimeout(String... selectors) {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));

        for (String selector : selectors) {
            try {
                WebElement element = shortWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(selector)));
                System.out.println("✅ Found element with selector: " + selector);
                return element;
            } catch (TimeoutException e) {
                System.out.println("❌ Selector not found within short timeout: " + selector);
            }
        }

        // If no specific error element found, check page for any text that might indicate error
        String pageText = driver.findElement(By.tagName("body")).getText().toLowerCase();
        if (pageText.contains("error") || pageText.contains("invalid") || pageText.contains("incorrect")) {
            System.out.println("⚠️ Page contains error-related text, but no specific error element found");
        }

        throw new NoSuchElementException("None of the error selectors worked: " + String.join(", ", selectors));
    }

    // Enhanced debug method
    private void captureDebugInfo() {
        try {
            System.out.println("=== DEBUG INFO ===");
            System.out.println("Current URL: " + driver.getCurrentUrl());
            System.out.println("Page title: " + driver.getTitle());
            System.out.println("Page source length: " + driver.getPageSource().length());

            // Take screenshot if possible
            if (driver instanceof TakesScreenshot) {
                try {
                    byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                    System.out.println("📸 Screenshot taken (size: " + screenshot.length + " bytes)");
                } catch (Exception screenshotEx) {
                    System.out.println("❌ Could not take screenshot: " + screenshotEx.getMessage());
                }
            }

            // Check for common elements
            String[] commonSelectors = {
                    "input", "button", "form", "[data-testid]", "[class*='error']", "[class*='success']"
            };

            for (String selector : commonSelectors) {
                try {
                    java.util.List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                    if (!elements.isEmpty()) {
                        System.out.println("Found " + elements.size() + " elements with selector: " + selector);
                        for (int i = 0; i < Math.min(elements.size(), 3); i++) {
                            WebElement elem = elements.get(i);
                            System.out.println("  - " + elem.getTagName() +
                                    " [visible: " + elem.isDisplayed() +
                                    ", text: '" + elem.getText().substring(0, Math.min(50, elem.getText().length())) + "']");
                        }
                    }
                } catch (Exception e) {
                    // Ignore selector errors during debug
                }
            }

        } catch (Exception debugEx) {
            System.err.println("Debug info collection failed: " + debugEx.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            try {
                // Clear all storage before closing
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript(
                        "localStorage.clear(); " +
                                "sessionStorage.clear(); " +
                                "document.cookie = 'auth=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';" +
                                "document.cookie = 'token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';"
                );

                driver.quit();
                System.out.println("✅ WebDriver closed successfully");
            } catch (Exception e) {
                System.out.println("⚠️ Error during driver cleanup: " + e.getMessage());
                // Force cleanup
                try {
                    driver.quit();
                } catch (Exception ex) {
                    // Ignore final cleanup errors
                }
            }
        }
    }
}