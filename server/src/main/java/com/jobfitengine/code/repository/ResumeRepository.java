package com.jobfitengine.code.repository;

import com.jobfitengine.code.entity.Resume;
import com.jobfitengine.code.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID> {
    Optional<Resume> findByUser(User user);
    Optional<Resume> findByUserAndId(User user, UUID id);
} 