package com.jobfitengine.code.dto;

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
    private UUID resume;
    
    @NotBlank(message = "Type is required")
    private String type; // "full-job" or "skills-section"
    
    @NotBlank(message = "Text area is required")
    private String textArea;
} 