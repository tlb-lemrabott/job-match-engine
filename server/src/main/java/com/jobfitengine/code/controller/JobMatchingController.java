package com.jobfitengine.code.controller;

import com.jobfitengine.code.dto.JobMatchingRequest;
import com.jobfitengine.code.dto.JobMatchingResponse;
import com.jobfitengine.code.entity.User;
import com.jobfitengine.code.service.JobMatchingService;
import com.jobfitengine.code.service.ResumeService;
import com.jobfitengine.code.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/job-matching")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class JobMatchingController {
    
    private final JobMatchingService jobMatchingService;
    private final UserService userService;
    private final ResumeService resumeService;
    
    @PostMapping
    public ResponseEntity<JobMatchingResponse> analyzeJobMatch(@Valid @RequestBody JobMatchingRequest request,
                                                             HttpServletRequest httpRequest) {
        try {
            User user = userService.findById((java.util.UUID) httpRequest.getAttribute("userId"))
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            log.info("Job matching analysis requested for user: {} with resume ID: {}", 
                    user.getEmail(), request.getResume());
            
            // Validate request type
            if (!request.getType().equals("full-job") && !request.getType().equals("skills-section")) {
                return ResponseEntity.badRequest()
                        .body(new JobMatchingResponse(false, "Invalid type. Must be 'full-job' or 'skills-section'", 
                                0.0, List.of(), List.of(), null));
            }
            
            // Get user's resume text
            var resumeOpt = resumeService.findByIdAndUser(request.getResume(), user);
            if (resumeOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new JobMatchingResponse(false, "Resume not found", 0.0, List.of(), List.of(), null));
            }
            
            String resumeText = resumeOpt.get().getExtractedText();
            if (resumeText == null || resumeText.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new JobMatchingResponse(false, "No text content found in resume", 0.0, List.of(), List.of(), null));
            }
            
            JobMatchingResponse response = jobMatchingService.performJobMatching(
                    resumeText,
                    user,
                    request.getTextArea(),
                    request.getType()
            );
            
            if (response.isSuccess()) {
                log.info("Job matching analysis completed successfully for user: {}", user.getEmail());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Job matching analysis failed for user: {}", user.getEmail());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error performing job matching: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new JobMatchingResponse(false, "Error performing job matching: " + e.getMessage(), 
                            0.0, List.of(), List.of(), null));
        }
    }
} 