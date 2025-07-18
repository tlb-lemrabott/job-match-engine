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
    
    @PutMapping("/update")
    public ResponseEntity<ResumeResponse> updateResume(@RequestParam("resume") MultipartFile file,
                                                      HttpServletRequest request) {
        try {
            UUID userId = (UUID) request.getAttribute("userId");
            
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(new ResumeResponse(false, "Authentication failed: No user ID found", null));
            }
            
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            log.info("Resume update attempt for user: {}", user.getEmail());
            
            ResumeResponse response = resumeService.updateResume(file, user);
            
            if (response.isSuccess()) {
                log.info("Resume updated successfully for user: {}", user.getEmail());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Resume update failed for user: {} - {}", user.getEmail(), response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error updating resume: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResumeResponse(false, "Error updating resume: " + e.getMessage(), null));
        }
    }

    @GetMapping("/download/{resumeId}")
    public ResponseEntity<?> downloadResume(@PathVariable UUID resumeId, HttpServletRequest request) {
        try {
            UUID userId = (UUID) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(403).body("Authentication failed: No user ID found");
            }
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            var resumeOpt = resumeService.findByIdAndUser(resumeId, user);
            if (resumeOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Resume not found");
            }
            var resume = resumeOpt.get();
            java.nio.file.Path filePath = java.nio.file.Paths.get(resume.getFilePath());
            if (!java.nio.file.Files.exists(filePath)) {
                return ResponseEntity.status(404).body("File not found");
            }
            org.springframework.core.io.Resource fileResource = new org.springframework.core.io.UrlResource(filePath.toUri());
            String contentType = "application/octet-stream";
            if (resume.getFileType().equalsIgnoreCase("pdf")) contentType = "application/pdf";
            if (resume.getFileType().equalsIgnoreCase("doc")) contentType = "application/msword";
            if (resume.getFileType().equalsIgnoreCase("docx")) contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resume.getFileName() + "\"")
                    .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, contentType)
                    .body(fileResource);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error downloading resume: " + e.getMessage());
        }
    }
} 