package com.example.Backend.repository;

import com.example.Backend.entity.ProjectFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectFavoriteRepository extends JpaRepository<ProjectFavorite, Long> {

    boolean existsByUserIdAndProjectId(Long userId, Long projectId);

    Optional<ProjectFavorite> findByUserIdAndProjectId(Long userId, Long projectId);

    List<ProjectFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);
}
