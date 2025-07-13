package com.jobfitengine.code.service;

import com.jobfitengine.code.dto.JobMatchingResponse;
import com.jobfitengine.code.entity.Resume;
import com.jobfitengine.code.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobMatchingService {
    
    private final AwsComprehendService comprehendService;
    private final BedrockEmbeddingService embeddingService;
    private final ResumeService resumeService;
    
    public JobMatchingResponse performJobMatching(UUID resumeId, User user, String jobDescription, String type) {
        try {
            // Get user's resume
            Optional<Resume> resumeOpt = resumeService.findByIdAndUser(resumeId, user);
            if (resumeOpt.isEmpty()) {
                return new JobMatchingResponse(false, "Resume not found", 0.0, 
                        List.of(), List.of(), null);
            }
            
            Resume resume = resumeOpt.get();
            String resumeText = resume.getExtractedText();
            
            if (resumeText == null || resumeText.trim().isEmpty()) {
                return new JobMatchingResponse(false, "No text content found in resume", 0.0, 
                        List.of(), List.of(), null);
            }
            
            // Extract skills from resume and job description
            List<String> resumeSkills = comprehendService.extractSkills(resumeText);
            List<String> jobSkills = comprehendService.extractSkills(jobDescription);
            
            // Extract technical terms
            List<String> resumeTechnicalTerms = comprehendService.extractTechnicalTerms(resumeText);
            List<String> jobTechnicalTerms = comprehendService.extractTechnicalTerms(jobDescription);
            
            // Combine all skills
            Set<String> allResumeSkills = new HashSet<>();
            allResumeSkills.addAll(resumeSkills);
            allResumeSkills.addAll(resumeTechnicalTerms);
            
            Set<String> allJobSkills = new HashSet<>();
            allJobSkills.addAll(jobSkills);
            allJobSkills.addAll(jobTechnicalTerms);
            
            // Calculate semantic similarity for overall matching
            double semanticSimilarity = embeddingService.calculateSemanticSimilarity(resumeText, jobDescription);
            
            // Find matched skills
            List<JobMatchingResponse.MatchedSkill> matchedSkills = findMatchedSkills(allResumeSkills, allJobSkills);
            
            // Find missing skills
            List<JobMatchingResponse.MissingSkill> missingSkills = findMissingSkills(allResumeSkills, allJobSkills);
            
            // Calculate overall matching score
            double matchingScore = calculateMatchingScore(matchedSkills, missingSkills, semanticSimilarity);
            
            // Generate analysis
            JobMatchingResponse.Analysis analysis = generateAnalysis(matchingScore, matchedSkills, missingSkills);
            
            return new JobMatchingResponse(
                    true,
                    "Job matching analysis completed successfully",
                    matchingScore,
                    matchedSkills,
                    missingSkills,
                    analysis
            );
            
        } catch (Exception e) {
            log.error("Error performing job matching: {}", e.getMessage());
            return new JobMatchingResponse(false, "Error performing job matching: " + e.getMessage(), 
                    0.0, List.of(), List.of(), null);
        }
    }
    
    private List<JobMatchingResponse.MatchedSkill> findMatchedSkills(Set<String> resumeSkills, Set<String> jobSkills) {
        return resumeSkills.stream()
                .filter(resumeSkill -> jobSkills.stream()
                        .anyMatch(jobSkill -> isSkillMatch(resumeSkill, jobSkill)))
                .map(skill -> new JobMatchingResponse.MatchedSkill(skill, calculateConfidence(skill), "Technical"))
                .collect(Collectors.toList());
    }
    
    private List<JobMatchingResponse.MissingSkill> findMissingSkills(Set<String> resumeSkills, Set<String> jobSkills) {
        return jobSkills.stream()
                .filter(jobSkill -> resumeSkills.stream()
                        .noneMatch(resumeSkill -> isSkillMatch(resumeSkill, jobSkill)))
                .map(skill -> new JobMatchingResponse.MissingSkill(skill, calculateImportance(skill), "Technical"))
                .sorted((s1, s2) -> Double.compare(s2.getImportance(), s1.getImportance()))
                .collect(Collectors.toList());
    }
    
    private boolean isSkillMatch(String skill1, String skill2) {
        String normalized1 = skill1.toLowerCase().trim();
        String normalized2 = skill2.toLowerCase().trim();
        
        // Exact match
        if (normalized1.equals(normalized2)) {
            return true;
        }
        
        // Contains match
        if (normalized1.contains(normalized2) || normalized2.contains(normalized1)) {
            return true;
        }
        
        // Handle common variations
        Map<String, List<String>> skillVariations = Map.of(
                "java", List.of("j2ee", "jee", "spring", "hibernate"),
                "javascript", List.of("js", "es6", "node", "react", "angular", "vue"),
                "python", List.of("django", "flask", "pandas", "numpy"),
                "aws", List.of("amazon web services", "ec2", "s3", "lambda"),
                "docker", List.of("containerization", "kubernetes", "k8s")
        );
        
        for (Map.Entry<String, List<String>> entry : skillVariations.entrySet()) {
            if ((normalized1.equals(entry.getKey()) && entry.getValue().contains(normalized2)) ||
                (normalized2.equals(entry.getKey()) && entry.getValue().contains(normalized1))) {
                return true;
            }
        }
        
        return false;
    }
    
    private double calculateConfidence(String skill) {
        // Simple confidence calculation based on skill length and commonality
        double baseConfidence = 0.7;
        
        // Increase confidence for longer, more specific skills
        if (skill.length() > 10) {
            baseConfidence += 0.1;
        }
        
        // Increase confidence for common technical skills
        String[] commonSkills = {"java", "python", "javascript", "aws", "docker", "kubernetes", "spring", "react"};
        for (String commonSkill : commonSkills) {
            if (skill.toLowerCase().contains(commonSkill)) {
                baseConfidence += 0.1;
                break;
            }
        }
        
        return Math.min(baseConfidence, 1.0);
    }
    
    private double calculateImportance(String skill) {
        // Simple importance calculation
        double baseImportance = 0.6;
        
        // Increase importance for common technical skills
        String[] importantSkills = {"java", "python", "javascript", "aws", "docker", "kubernetes", "spring", "react", "sql"};
        for (String importantSkill : importantSkills) {
            if (skill.toLowerCase().contains(importantSkill)) {
                baseImportance += 0.2;
                break;
            }
        }
        
        // Increase importance for longer, more specific skills
        if (skill.length() > 8) {
            baseImportance += 0.1;
        }
        
        return Math.min(baseImportance, 1.0);
    }
    
    private double calculateMatchingScore(List<JobMatchingResponse.MatchedSkill> matchedSkills, 
                                        List<JobMatchingResponse.MissingSkill> missingSkills, 
                                        double semanticSimilarity) {
        if (matchedSkills.isEmpty() && missingSkills.isEmpty()) {
            return semanticSimilarity * 100;
        }
        
        double matchedScore = matchedSkills.stream()
                .mapToDouble(skill -> skill.getConfidence())
                .average()
                .orElse(0.0);
        
        double missingPenalty = missingSkills.stream()
                .mapToDouble(skill -> skill.getImportance())
                .average()
                .orElse(0.0);
        
        // Weighted combination of semantic similarity and skill matching
        double skillScore = (matchedScore * 0.7) - (missingPenalty * 0.3);
        double finalScore = (semanticSimilarity * 0.4) + (skillScore * 0.6);
        
        return Math.max(0.0, Math.min(100.0, finalScore * 100));
    }
    
    private JobMatchingResponse.Analysis generateAnalysis(double matchingScore, 
                                                         List<JobMatchingResponse.MatchedSkill> matchedSkills,
                                                         List<JobMatchingResponse.MissingSkill> missingSkills) {
        String overallMatch;
        List<String> recommendations = new ArrayList<>();
        
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
        
        if (!missingSkills.isEmpty()) {
            recommendations.add("Prioritize learning: " + 
                    missingSkills.stream()
                            .limit(3)
                            .map(JobMatchingResponse.MissingSkill::getSkill)
                            .collect(Collectors.joining(", ")));
        }
        
        return new JobMatchingResponse.Analysis(overallMatch, recommendations);
    }
} 