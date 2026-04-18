package com.example.Backend.repository;

import com.example.Backend.entity.ProjectEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectEvaluationRepository extends JpaRepository<ProjectEvaluation, Long> {

    boolean existsByProjectIdAndTeacherId(Long projectId, Long teacherId);

    Optional<ProjectEvaluation> findByIdAndTeacherId(Long id, Long teacherId);

    List<ProjectEvaluation> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<ProjectEvaluation> findByProjectStudentUserEmailOrderByCreatedAtDesc(String email);
}
