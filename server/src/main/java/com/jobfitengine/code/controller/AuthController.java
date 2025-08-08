package com.jobfitengine.code.controller;

import com.jobfitengine.code.dto.AuthRequest;
import com.jobfitengine.code.dto.AuthResponse;
import com.jobfitengine.code.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    
    private final UserService userService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        log.info("Login attempt for user: {}", request.getEmail());
        
        AuthResponse response = userService.authenticateUser(request.getEmail(), request.getPassword());
        
        if (response.isSuccess()) {
            log.info("Login successful for user: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } else {
            log.warn("Login failed for user: {}", request.getEmail());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        log.info("Registration attempt for user: {}", request.getEmail());
        
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Name is required", null, null));
        }
        
        AuthResponse response = userService.registerUser(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhone()
        );
        
        if (response.isSuccess()) {
            log.info("Registration successful for user: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } else {
            log.warn("Registration failed for user: {}", request.getEmail());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 