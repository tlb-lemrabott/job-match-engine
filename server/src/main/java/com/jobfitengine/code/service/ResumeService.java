package com.jobfitengine.code.service;

import com.jobfitengine.code.dto.ResumeResponse;
import com.jobfitengine.code.entity.Resume;
import com.jobfitengine.code.entity.User;
import com.jobfitengine.code.repository.ResumeRepository;
import com.jobfitengine.code.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {
    
    private final ResumeRepository resumeRepository;
    private final DocumentTextExtractionService textExtractionService;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    
    @Value("${file.upload.path}")
    private String uploadPath;
    
    public ResumeResponse uploadResume(MultipartFile file, User user) {
        try {
            log.info("Starting resume upload for user: {}", user.getEmail());
            
            // Validate file
            if (file.isEmpty()) {
                log.warn("File is empty for user: {}", user.getEmail());
                return new ResumeResponse(false, "File is empty", null);
            }
            
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                log.warn("Invalid filename for user: {}", user.getEmail());
                return new ResumeResponse(false, "Invalid filename", null);
            }
            
            // Check file type
            String fileType = getFileExtension(originalFilename);
            if (!isValidFileType(fileType)) {
                log.warn("Invalid file type: {} for user: {}", fileType, user.getEmail());
                return new ResumeResponse(false, "Invalid file type. Only PDF, DOC, DOCX are allowed", null);
            }
            
            // Check file size (10MB limit)
            if (file.getSize() > 10 * 1024 * 1024) {
                log.warn("File size exceeds limit: {} bytes for user: {}", file.getSize(), user.getEmail());
                return new ResumeResponse(false, "File size exceeds 10MB limit", null);
            }
            
            // Create upload directory if it doesn't exist
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // Generate unique filename
            String uniqueFilename = UUID.randomUUID().toString() + "." + fileType;
            Path filePath = uploadDir.resolve(uniqueFilename);
            
            // Save file
            Files.copy(file.getInputStream(), filePath);
            
            // Extract text from document
            String extractedText = textExtractionService.extractText(file.getInputStream());
            
            // Delete existing resume if any
            resumeRepository.findByUser(user).ifPresent(existingResume -> {
                try {
                    Files.deleteIfExists(Paths.get(existingResume.getFilePath()));
                    resumeRepository.delete(existingResume);
                } catch (IOException e) {
                    log.error("Error deleting existing resume file: {}", e.getMessage());
                }
            });
            
            // Save resume record
            Resume resume = new Resume();
            resume.setFileName(originalFilename);
            resume.setFileSize(file.getSize());
            resume.setFileType(fileType);
            resume.setFilePath(filePath.toString());
            resume.setExtractedText(extractedText);
            resume.setUser(user);
            
            Resume savedResume = resumeRepository.save(resume);
            
            ResumeResponse.ResumeDto resumeDto = new ResumeResponse.ResumeDto(
                    savedResume.getId(),
                    savedResume.getFileName(),
                    savedResume.getFileSize(),
                    savedResume.getUploadDate(),
                    savedResume.getFileType(),
                    null // fileUrl is null for local storage
            );
            
            log.info("Resume upload completed successfully for user: {}", user.getEmail());
            return new ResumeResponse(true, "Resume uploaded successfully", resumeDto);
            
        } catch (IOException e) {
            log.error("Error uploading resume for user {}: {}", user.getEmail(), e.getMessage());
            return new ResumeResponse(false, "Error uploading resume: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("Unexpected error uploading resume for user {}: {}", user.getEmail(), e.getMessage());
            return new ResumeResponse(false, "Error uploading resume: " + e.getMessage(), null);
        }
    }
    
    public ResumeResponse updateResume(MultipartFile file, User user) {
        try {
            log.info("Starting resume update for user: {}", user.getEmail());
            
            // Check if user has an existing resume
            Optional<Resume> existingResumeOpt = resumeRepository.findByUser(user);
            if (existingResumeOpt.isEmpty()) {
                log.warn("No existing resume found for user: {}", user.getEmail());
                return new ResumeResponse(false, "No existing resume found. Please upload a resume first.", null);
            }
            
            // Validate file
            if (file.isEmpty()) {
                log.warn("File is empty for user: {}", user.getEmail());
                return new ResumeResponse(false, "File is empty", null);
            }
            
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                log.warn("Invalid filename for user: {}", user.getEmail());
                return new ResumeResponse(false, "Invalid filename", null);
            }
            
            // Check file type
            String fileType = getFileExtension(originalFilename);
            if (!isValidFileType(fileType)) {
                log.warn("Invalid file type: {} for user: {}", fileType, user.getEmail());
                return new ResumeResponse(false, "Invalid file type. Only PDF, DOC, DOCX are allowed", null);
            }
            
            // Check file size (10MB limit)
            if (file.getSize() > 10 * 1024 * 1024) {
                log.warn("File size exceeds limit: {} bytes for user: {}", file.getSize(), user.getEmail());
                return new ResumeResponse(false, "File size exceeds 10MB limit", null);
            }
            
            // Create upload directory if it doesn't exist
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // Generate unique filename for new file
            String uniqueFilename = UUID.randomUUID().toString() + "." + fileType;
            Path newFilePath = uploadDir.resolve(uniqueFilename);
            
            // Save new file
            Files.copy(file.getInputStream(), newFilePath);
            
            // Extract text from new document
            String extractedText = textExtractionService.extractText(file.getInputStream());
            
            // Get existing resume and delete old file
            Resume existingResume = existingResumeOpt.get();
            try {
                Files.deleteIfExists(Paths.get(existingResume.getFilePath()));
            } catch (IOException e) {
                log.error("Error deleting old resume file: {}", e.getMessage());
            }
            
            // Update resume record
            existingResume.setFileName(originalFilename);
            existingResume.setFileSize(file.getSize());
            existingResume.setFileType(fileType);
            existingResume.setFilePath(newFilePath.toString());
            existingResume.setExtractedText(extractedText);
            existingResume.setUploadDate(java.time.LocalDateTime.now());
            
            Resume updatedResume = resumeRepository.save(existingResume);
            
            ResumeResponse.ResumeDto resumeDto = new ResumeResponse.ResumeDto(
                    updatedResume.getId(),
                    updatedResume.getFileName(),
                    updatedResume.getFileSize(),
                    updatedResume.getUploadDate(),
                    updatedResume.getFileType(),
                    null // fileUrl is null for local storage
            );
            
            log.info("Resume update completed successfully for user: {}", user.getEmail());
            return new ResumeResponse(true, "Resume updated successfully", resumeDto);
            
        } catch (IOException e) {
            log.error("Error updating resume for user {}: {}", user.getEmail(), e.getMessage());
            return new ResumeResponse(false, "Error updating resume: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("Unexpected error updating resume for user {}: {}", user.getEmail(), e.getMessage());
            return new ResumeResponse(false, "Error updating resume: " + e.getMessage(), null);
        }
    }
    
    public ResumeResponse getUserResume(User user) {
        Optional<Resume> resumeOpt = resumeRepository.findByUser(user);
        
        if (resumeOpt.isEmpty()) {
            return new ResumeResponse(false, "No resume found for user", null);
        }
        
        Resume resume = resumeOpt.get();
        ResumeResponse.ResumeDto resumeDto = new ResumeResponse.ResumeDto(
                resume.getId(),
                resume.getFileName(),
                resume.getFileSize(),
                resume.getUploadDate(),
                resume.getFileType(),
                null // fileUrl is null for local storage
        );
        
        return new ResumeResponse(true, "Resume retrieved successfully", resumeDto);
    }
    
    @Transactional
    public ResumeResponse deleteUserResume(User user) {
        log.info("Starting resume deletion for user: {}", user.getEmail());
        
        Optional<Resume> resumeOpt = resumeRepository.findByUser(user);
        
        if (resumeOpt.isEmpty()) {
            log.warn("No resume found for user: {}", user.getEmail());
            return new ResumeResponse(false, "No resume found for user", null);
        }
        
        Resume resume = resumeOpt.get();
        UUID resumeId = resume.getId();
        log.info("Found resume to delete - ID: {}, File: {}", resumeId, resume.getFileName());
        
        try {
            // Delete file from filesystem first
            Path filePath = Paths.get(resume.getFilePath());
            boolean fileDeleted = Files.deleteIfExists(filePath);
            log.info("File deletion result: {} for path: {}", fileDeleted, filePath);
            
            // Refresh the user entity to ensure it's in the correct state
            entityManager.refresh(user);
            
            // Clear the user's resume reference
            user.setResume(null);
            entityManager.merge(user);
            
            // Delete the resume entity
            entityManager.remove(resume);
            entityManager.flush();
            
            log.info("Resume record deleted from database for user: {}", user.getEmail());
            
            return new ResumeResponse(true, "Resume deleted successfully", null);
            
        } catch (IOException e) {
            log.error("Error deleting resume file for user {}: {}", user.getEmail(), e.getMessage());
            return new ResumeResponse(false, "Error deleting resume: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("Unexpected error deleting resume for user {}: {}", user.getEmail(), e.getMessage());
            return new ResumeResponse(false, "Error deleting resume: " + e.getMessage(), null);
        }
    }
    
    public Optional<Resume> findByIdAndUser(UUID resumeId, User user) {
        return resumeRepository.findByUserAndId(user, resumeId);
    }
    
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1).toLowerCase() : "";
    }
    
    private boolean isValidFileType(String fileType) {
        return fileType.equals("pdf") || fileType.equals("doc") || fileType.equals("docx");
    }
} 