package com.example.Backend.repository;

import com.example.Backend.entity.ProjectPurchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectPurchaseRepository extends JpaRepository<ProjectPurchase, Long> {
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    List<ProjectPurchase> findByUserIdOrderByCreatedAtDesc(Long userId);
}
