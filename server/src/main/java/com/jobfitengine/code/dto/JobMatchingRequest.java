package com.jobfitengine.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobMatchingRequest {
    
    @NotNull(message = "Resume ID is required")
    @JsonProperty("resume")
    private String resumeId; // Accept as string, convert to UUID in service
    
    @NotBlank(message = "Type is required")
    private String type; // "full-job" or "skills-section"
    
    @NotBlank(message = "Text area is required")
    private String textArea;
    
    // Helper method to get UUID
    public UUID getResumeUUID() {
        try {
            return UUID.fromString(resumeId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid resume ID format: " + resumeId);
        }
    }
} 