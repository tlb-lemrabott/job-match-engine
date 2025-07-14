package com.jobfitengine.code.controller;

import com.jobfitengine.code.dto.ResumeResponse;
import com.jobfitengine.code.entity.User;
import com.jobfitengine.code.service.ResumeService;
import com.jobfitengine.code.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/resume")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class ResumeController {
    
    private final ResumeService resumeService;
    private final UserService userService;
    
    @PostMapping("/upload")
    public ResponseEntity<ResumeResponse> uploadResume(@RequestParam("resume") MultipartFile file,
                                                      HttpServletRequest request) {
        try {
            UUID userId = (UUID) request.getAttribute("userId");
            
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(new ResumeResponse(false, "Authentication failed: No user ID found", null));
            }
            
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            log.info("Resume upload attempt for user: {}", user.getEmail());
            
            ResumeResponse response = resumeService.uploadResume(file, user);
            
            if (response.isSuccess()) {
                log.info("Resume uploaded successfully for user: {}", user.getEmail());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Resume upload failed for user: {}", user.getEmail());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error uploading resume: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResumeResponse(false, "Error uploading resume: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/user")
    public ResponseEntity<ResumeResponse> getUserResume(HttpServletRequest request) {
        try {
            UUID userId = (UUID) request.getAttribute("userId");
            
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(new ResumeResponse(false, "Authentication failed: No user ID found", null));
            }
            
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            log.info("Getting resume for user: {}", user.getEmail());
            
            ResumeResponse response = resumeService.getUserResume(user);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error getting user resume: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResumeResponse(false, "Error getting resume: " + e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/user")
    public ResponseEntity<ResumeResponse> deleteUserResume(HttpServletRequest request) {
        try {
            UUID userId = (UUID) request.getAttribute("userId");
            
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(new ResumeResponse(false, "Authentication failed: No user ID found", null));
            }
            
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            log.info("Deleting resume for user: {}", user.getEmail());
            
            ResumeResponse response = resumeService.deleteUserResume(user);
            
            if (response.isSuccess()) {
                log.info("Resume deleted successfully for user: {}", user.getEmail());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Resume deletion failed for user: {} - {}", user.getEmail(), response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error deleting user resume: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResumeResponse(false, "Error deleting resume: " + e.getMessage(), null));
        }
    }
} 