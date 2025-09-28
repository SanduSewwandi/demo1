package com.example.demo1.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginUITest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setup() {
        io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        String ciEnv = System.getenv("CI");
        if ("true".equals(ciEnv)) {
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
        }

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.manage().window().maximize();
    }

    @Test
    void testInvalidCredentialsErrorMessage() {
        driver.get("http://localhost:5173/login");

        // Fill with invalid credentials (like in GreenScape screenshot)
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='email']"))).sendKeys("tesl@example.com");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("wrongpassword");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Wait for error message like "Invalid credentials"
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Invalid') or contains(text(),'invalid')]")
        ));
        Assertions.assertTrue(errorMessage.isDisplayed());
        Assertions.assertTrue(errorMessage.getText().toLowerCase().contains("invalid"));
    }

    @Test
    void testSuccessfulLogin() {
        driver.get("http://localhost:5173/login");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='email']"))).sendKeys("test@example.com");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("Password123!");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Wait for success - either redirect or success message
        try {
            // Check if redirected away from login page
            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
            Assertions.assertFalse(driver.getCurrentUrl().contains("/login"));
        } catch (Exception e) {
            // If still on login page, check for success message
            WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'Welcome') or contains(text(),'Success')]")
            ));
            Assertions.assertTrue(successMessage.isDisplayed());
        }
    }

    @Test
    void testEmptyCredentialsValidation() {
        driver.get("http://localhost:5173/login");

        // Click login with empty fields
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Check for validation errors
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".error, [role='alert'], .text-danger")
        ));
        Assertions.assertTrue(errorMessage.isDisplayed());
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}