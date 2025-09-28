package com.example.demo1.controller;



import com.example.demo1.model.*;
import com.example.demo1.service.UserService;
import com.example.demo1.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            String token = jwtService.generateToken(registeredUser.getEmail(), registeredUser.getRole().name());

            // Create user response without password
            User userResponse = new User();
            userResponse.setId(registeredUser.getId());
            userResponse.setName(registeredUser.getName());
            userResponse.setEmail(registeredUser.getEmail());
            userResponse.setRole(registeredUser.getRole());

            AuthResponse response = new AuthResponse(true, token, registeredUser.getRole().name(),
                    userResponse, "User registered successfully");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login/user")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            var user = userService.findByEmail(loginRequest.getEmail())
                    .orElse(null);

            if (user == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "User doesn't exist")
                );
            }

            if (!userService.validatePassword(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "Invalid credentials")
                );
            }

            String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

            // Create user response without password
            User userResponse = new User();
            userResponse.setId(user.getId());
            userResponse.setName(user.getName());
            userResponse.setEmail(user.getEmail());
            userResponse.setRole(user.getRole());

            AuthResponse response = new AuthResponse(true, token, user.getRole().name(),
                    userResponse, "User login successful");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            if (adminEmail.equals(loginRequest.getEmail()) && adminPassword.equals(loginRequest.getPassword())) {
                String token = jwtService.generateToken(loginRequest.getEmail(), "ADMIN");

                AuthResponse response = new AuthResponse(true, token, "ADMIN", null, "Admin login successful");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "Invalid admin credentials")
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }
}