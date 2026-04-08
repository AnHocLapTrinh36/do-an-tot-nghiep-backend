package com.example.Backend.repository;

import com.example.Backend.entity.JobFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobFavoriteRepository extends JpaRepository<JobFavorite, Long> {
    boolean existsByUserIdAndJobPostId(Long userId, Long jobPostId);

    Optional<JobFavorite> findByUserIdAndJobPostId(Long userId, Long jobPostId);

    List<JobFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);
}
