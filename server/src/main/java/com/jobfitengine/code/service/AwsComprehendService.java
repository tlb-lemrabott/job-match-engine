package com.jobfitengine.code.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.comprehend.model.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AwsComprehendService {
    
    @Value("${aws.region}")
    private String region;
    
    private ComprehendClient comprehendClient;
    
    private ComprehendClient getComprehendClient() {
        if (comprehendClient == null) {
            comprehendClient = ComprehendClient.builder()
                    .region(Region.of(region))
                    .build();
        }
        return comprehendClient;
    }
    
    public List<String> extractSkills(String text) {
        try {
            // Use Key Phrases detection to identify skills
            DetectKeyPhrasesRequest request = DetectKeyPhrasesRequest.builder()
                    .text(text)
                    .languageCode(LanguageCode.EN)
                    .build();
            
            DetectKeyPhrasesResponse response = getComprehendClient().detectKeyPhrases(request);
            
            List<String> skills = response.keyPhrases().stream()
                    .map(KeyPhrase::text)
                    .filter(this::isLikelySkill)
                    .distinct()
                    .collect(Collectors.toList());
            
            log.info("Extracted {} skills from text", skills.size());
            return skills;
            
        } catch (Exception e) {
            log.error("Error extracting skills from text: {}", e.getMessage());
            return List.of();
        }
    }
    
    public List<String> extractEntities(String text) {
        try {
            DetectEntitiesRequest request = DetectEntitiesRequest.builder()
                    .text(text)
                    .languageCode(LanguageCode.EN)
                    .build();
            
            DetectEntitiesResponse response = getComprehendClient().detectEntities(request);
            
            List<String> entities = response.entities().stream()
                    .map(Entity::text)
                    .distinct()
                    .collect(Collectors.toList());
            
            log.info("Extracted {} entities from text", entities.size());
            return entities;
            
        } catch (Exception e) {
            log.error("Error extracting entities from text: {}", e.getMessage());
            return List.of();
        }
    }
    
    public List<String> extractTechnicalTerms(String text) {
        try {
            // Use syntax analysis to identify technical terms
            DetectSyntaxRequest request = DetectSyntaxRequest.builder()
                    .text(text)
                    .languageCode(SyntaxLanguageCode.EN)
                    .build();
            
            DetectSyntaxResponse response = getComprehendClient().detectSyntax(request);
            
            List<String> technicalTerms = response.syntaxTokens().stream()
                    .filter(token -> isTechnicalTerm(token))
                    .map(SyntaxToken::text)
                    .distinct()
                    .collect(Collectors.toList());
            
            log.info("Extracted {} technical terms from text", technicalTerms.size());
            return technicalTerms;
            
        } catch (Exception e) {
            log.error("Error extracting technical terms from text: {}", e.getMessage());
            return List.of();
        }
    }
    
    private boolean isLikelySkill(String phrase) {
        // Filter for likely skills - technical terms, programming languages, frameworks, etc.
        String lowerPhrase = phrase.toLowerCase();
        
        // Common technical skills
        String[] technicalSkills = {
            "java", "python", "javascript", "typescript", "c++", "c#", "php", "ruby", "go", "rust",
            "spring", "react", "angular", "vue", "node.js", "express", "django", "flask", "laravel",
            "aws", "azure", "gcp", "docker", "kubernetes", "jenkins", "git", "sql", "mongodb",
            "redis", "elasticsearch", "kafka", "rabbitmq", "microservices", "rest", "graphql",
            "html", "css", "bootstrap", "tailwind", "jquery", "webpack", "babel", "jest", "junit"
        };
        
        for (String skill : technicalSkills) {
            if (lowerPhrase.contains(skill)) {
                return true;
            }
        }
        
        // Check for patterns like "X years of experience" or "proficient in X"
        return lowerPhrase.matches(".*\\d+\\s*(years?|yrs?).*") ||
               lowerPhrase.matches(".*(proficient|experienced|skilled|expert).*") ||
               lowerPhrase.length() > 2 && lowerPhrase.length() < 50;
    }
    
    private boolean isTechnicalTerm(SyntaxToken token) {
        // Filter for nouns, proper nouns, and other technical terms
        PartOfSpeechTagType tag = token.partOfSpeech().tag();
        return tag == PartOfSpeechTagType.NOUN || 
               tag == PartOfSpeechTagType.PROPN || 
               tag == PartOfSpeechTagType.ADJ;
    }
} 