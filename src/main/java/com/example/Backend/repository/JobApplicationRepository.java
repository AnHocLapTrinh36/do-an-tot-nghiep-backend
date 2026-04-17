package com.example.Backend.repository;

import com.example.Backend.entity.ApplicationStatus;
import com.example.Backend.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByStudentIdAndJobPostId(Long studentId, Long jobPostId);

    List<JobApplication> findByJobPostId(Long jobPostId);

    List<JobApplication> findByJobPostIdAndStatus(Long jobPostId, ApplicationStatus status);
}