package com.jobfitengine.code.service;

import com.jobfitengine.code.dto.JobMatchingResponse;
import com.jobfitengine.code.entity.Resume;
import com.jobfitengine.code.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobMatchingService {
    
    private final ResumeService resumeService;
    private final WebClient webClient;
    
    @Value("${python.matcher.url:http://localhost:5000}")
    private String pythonMatcherUrl;
    
    public JobMatchingResponse performJobMatching(String resumeText, User user, String jobDescription, String type) {
        try {
            log.info("Starting job matching analysis for user: {}", user.getEmail());
            
            if (resumeText == null || resumeText.trim().isEmpty()) {
                return new JobMatchingResponse(false, "No resume text provided", 0.0, 
                        List.of(), List.of(), null);
            }
            
            if (jobDescription == null || jobDescription.trim().isEmpty()) {
                return new JobMatchingResponse(false, "No job description provided", 0.0, 
                        List.of(), List.of(), null);
            }
            
            // Call Python service for analysis
            PythonMatchResponse pythonResponse = callPythonMatcher(resumeText, jobDescription);
            
            if (pythonResponse == null) {
                return new JobMatchingResponse(false, "Failed to get analysis from Python service", 0.0, 
                        List.of(), List.of(), null);
            }
            
            // Convert Python response to Java response format
            JobMatchingResponse response = convertPythonResponse(pythonResponse);
            
            log.info("Job matching analysis completed for user: {}. Score: {}", 
                    user.getEmail(), response.getMatchingScore());
            
            return response;
            
        } catch (Exception e) {
            log.error("Error performing job matching for user {}: {}", user.getEmail(), e.getMessage());
            return new JobMatchingResponse(false, "Error performing job matching: " + e.getMessage(), 
                    0.0, List.of(), List.of(), null);
        }
    }
    
    private PythonMatchResponse callPythonMatcher(String resumeText, String jobDescription) {
        try {
            Map<String, String> request = Map.of(
                "resume_text", resumeText,
                "job_description", jobDescription
            );
            
            return webClient.post()
                .uri(pythonMatcherUrl + "/analyze")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PythonMatchResponse.class)
                .block();
                
        } catch (Exception e) {
            log.error("Error calling Python matcher service: {}", e.getMessage());
            return null;
        }
    }
    
    private JobMatchingResponse convertPythonResponse(PythonMatchResponse pythonResponse) {
        // Convert matched skills
        List<JobMatchingResponse.MatchedSkill> matchedSkills = pythonResponse.getMatchedSkills().stream()
                .map(skill -> new JobMatchingResponse.MatchedSkill(skill, 0.8, "Technical"))
                .toList();
        
        // Convert missing skills
        List<JobMatchingResponse.MissingSkill> missingSkills = pythonResponse.getMissingSkills().stream()
                .map(skill -> new JobMatchingResponse.MissingSkill(skill, 0.7, "Technical"))
                .toList();
        
        // Generate analysis based on score
        JobMatchingResponse.Analysis analysis = generateAnalysis(
                pythonResponse.getMatchScore(), 
                matchedSkills, 
                missingSkills,
                pythonResponse.getMissingExperience()
        );
        
        return new JobMatchingResponse(
                true,
                "Job matching analysis completed successfully",
                pythonResponse.getMatchScore(),
                matchedSkills,
                missingSkills,
                analysis
        );
    }
    
    private JobMatchingResponse.Analysis generateAnalysis(double matchingScore, 
                                                         List<JobMatchingResponse.MatchedSkill> matchedSkills,
                                                         List<JobMatchingResponse.MissingSkill> missingSkills,
                                                         List<String> missingExperience) {
        String overallMatch;
        List<String> recommendations = new java.util.ArrayList<>();
        
        if (matchingScore >= 80) {
            overallMatch = "Excellent match! Your profile strongly aligns with the job requirements.";
            recommendations.add("Highlight your matched skills prominently in your application");
            recommendations.add("Prepare to discuss your experience with the identified technologies");
        } else if (matchingScore >= 60) {
            overallMatch = "Good match. You have many of the required skills but some gaps exist.";
            recommendations.add("Focus on learning the missing skills identified");
            recommendations.add("Emphasize transferable skills and experience");
        } else if (matchingScore >= 40) {
            overallMatch = "Moderate match. Consider developing additional skills before applying.";
            recommendations.add("Invest time in learning the missing technical skills");
            recommendations.add("Consider similar roles with fewer requirements");
        } else {
            overallMatch = "Low match. This role may require significant skill development.";
            recommendations.add("Focus on building the core missing skills first");
            recommendations.add("Consider entry-level positions or internships");
        }
        
        // Add missing experience recommendations
        if (!missingExperience.isEmpty()) {
            recommendations.add("Experience gaps: " + String.join(", ", missingExperience));
        }
        
        if (!missingSkills.isEmpty()) {
            recommendations.add("Prioritize learning: " + 
                    missingSkills.stream()
                            .limit(3)
                            .map(JobMatchingResponse.MissingSkill::getSkill)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse(""));
        }
        
        return new JobMatchingResponse.Analysis(overallMatch, recommendations);
    }
    
    // Inner class to represent Python service response
    public static class PythonMatchResponse {
        private double matchScore;
        private List<String> matchedSkills;
        private List<String> missingSkills;
        private List<String> missingExperience;
        private List<String> otherMissing;
        
        // Getters and setters
        public double getMatchScore() { return matchScore; }
        public void setMatchScore(double matchScore) { this.matchScore = matchScore; }
        
        public List<String> getMatchedSkills() { return matchedSkills; }
        public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }
        
        public List<String> getMissingSkills() { return missingSkills; }
        public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }
        
        public List<String> getMissingExperience() { return missingExperience; }
        public void setMissingExperience(List<String> missingExperience) { this.missingExperience = missingExperience; }
        
        public List<String> getOtherMissing() { return otherMissing; }
        public void setOtherMissing(List<String> otherMissing) { this.otherMissing = otherMissing; }
    }
} 