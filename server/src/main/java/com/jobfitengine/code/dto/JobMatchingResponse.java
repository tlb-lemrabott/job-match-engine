package com.jobfitengine.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobMatchingResponse {
    private boolean success;
    private String message;
    private double matchingScore;
    private List<MatchedSkill> matchedSkills;
    private List<MissingSkill> missingSkills;
    private Analysis analysis;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchedSkill {
        private String skill;
        private double confidence;
        private String category;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MissingSkill {
        private String skill;
        private double importance;
        private String category;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Analysis {
        private String overallMatch;
        private List<String> recommendations;
    }
} 