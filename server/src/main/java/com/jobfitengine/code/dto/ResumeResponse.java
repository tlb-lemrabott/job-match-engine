package com.jobfitengine.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeResponse {
    private boolean success;
    private String message;
    private ResumeDto resume;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumeDto {
        private UUID id;
        private String fileName;
        private Long fileSize;
        private LocalDateTime uploadDate;
        private String fileType;
        private String fileUrl;
    }
} 