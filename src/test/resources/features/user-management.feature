Feature: User Management
  As a user
  I want to manage my account
  So that I can securely access the application

  Background:
    Given the application is running
    And I have access to user service

  @Positive @Registration
  Scenario: Successful user registration with valid credentials
    Given I have valid user registration data
    When I register a new user
    Then the registration should be successful
    And the user should be saved in the database

  @Negative @Registration
  Scenario: User registration with duplicate email should fail
    Given a user already exists with email "existing@example.com"
    And I have valid user registration data
    When I try to register another user with the same email
    Then the registration should fail with error "User already exists"

  @Negative @Registration
  Scenario: User registration with weak password should fail
    Given I have user data with weak password "123"
    When I try to register the user
    Then the registration should fail with error "Password must be at least 8 characters"

  @Positive @Authentication
  Scenario: Successful user login with correct credentials
    Given a registered user with email "test@example.com" and password "Password123"
    When I login with correct credentials
    Then the login should be successful
    And I should receive user details

  @Negative @Authentication
  Scenario: User login with incorrect password should fail
    Given a registered user with email "test@example.com" and password "Password123"
    When I login with incorrect password "WrongPassword"
    Then the login should fail with error "Invalid credentials"