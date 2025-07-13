package com.jobfitengine.code.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class DocumentTextExtractionService {
    
    private final Tika tika;
    
    public DocumentTextExtractionService() {
        this.tika = new Tika();
        // Tika will handle large files automatically
    }
    
    public String extractText(InputStream inputStream) {
        try {
            String extractedText = tika.parseToString(inputStream);
            
            // Clean up the extracted text
            if (extractedText != null) {
                extractedText = extractedText.trim();
                // Remove excessive whitespace and normalize line breaks
                extractedText = extractedText.replaceAll("\\s+", " ");
                extractedText = extractedText.replaceAll("\\n\\s*\\n", "\n");
            }
            
            log.info("Successfully extracted text from document. Length: {}", 
                    extractedText != null ? extractedText.length() : 0);
            
            return extractedText != null ? extractedText : "";
            
        } catch (IOException | TikaException e) {
            log.error("Error extracting text from document: {}", e.getMessage());
            throw new RuntimeException("Failed to extract text from document", e);
        }
    }
} 