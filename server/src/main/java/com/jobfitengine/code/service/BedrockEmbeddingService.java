package com.jobfitengine.code.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BedrockEmbeddingService {
    
    private final BedrockRuntimeClient bedrockClient;
    private final ObjectMapper objectMapper;
    
    @Value("${aws.region}")
    private String region;
    
    public BedrockEmbeddingService(BedrockRuntimeClient bedrockClient) {
        this.bedrockClient = bedrockClient;
        this.objectMapper = new ObjectMapper();
    }
    
    public List<Double> generateEmbedding(String text) {
        try {
            // Prepare the request payload for Titan embedding model
            String requestBody = String.format(
                "{\"inputText\": \"%s\"}",
                text.replace("\"", "\\\"")
            );
            
            InvokeModelRequest request = InvokeModelRequest.builder()
                .modelId("amazon.titan-embed-text-v1")
                .body(software.amazon.awssdk.core.SdkBytes.fromUtf8String(requestBody))
                .build();
            
            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();
            
            // Parse the response to extract embedding
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            JsonNode embeddingNode = jsonResponse.get("embedding");
            
            if (embeddingNode != null && embeddingNode.isArray()) {
                List<Double> embedding = new ArrayList<>();
                for (JsonNode value : embeddingNode) {
                    embedding.add(value.asDouble());
                }
                log.info("Generated embedding with {} dimensions", embedding.size());
                return embedding;
            } else {
                log.warn("No embedding found in response: {}", responseBody);
                return List.of();
            }
            
        } catch (Exception e) {
            log.error("Error generating embedding: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    public double calculateCosineSimilarity(List<Double> embedding1, List<Double> embedding2) {
        if (embedding1.isEmpty() || embedding2.isEmpty() || embedding1.size() != embedding2.size()) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < embedding1.size(); i++) {
            double val1 = embedding1.get(i);
            double val2 = embedding2.get(i);
            
            dotProduct += val1 * val2;
            norm1 += val1 * val1;
            norm2 += val2 * val2;
        }
        
        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (norm1 * norm2);
    }
    
    public double calculateSemanticSimilarity(String text1, String text2) {
        List<Double> embedding1 = generateEmbedding(text1);
        List<Double> embedding2 = generateEmbedding(text2);
        
        return calculateCosineSimilarity(embedding1, embedding2);
    }
} 