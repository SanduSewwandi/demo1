package com.example.demo1.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = com.example.demo1.Demo1Application.class)
public class CucumberSpringConfiguration {
    // This class provides Spring context configuration for Cucumber tests
}