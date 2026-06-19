package com.example.Backend.repository;

import com.example.Backend.entity.ProjectEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectEvaluationRepository extends JpaRepository<ProjectEvaluation, Long> {

    boolean existsByProjectIdAndTeacherId(Long projectId, Long teacherId);

    Optional<ProjectEvaluation> findByProjectIdAndTeacherId(Long projectId, Long teacherId);

    Optional<ProjectEvaluation> findByIdAndTeacherId(Long id, Long teacherId);

    List<ProjectEvaluation> findByTeacherId(Long teacherId);

    List<ProjectEvaluation> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    @Query("""
            SELECT pe
            FROM ProjectEvaluation pe
            WHERE pe.project.student.user.email = :email
               OR EXISTS (
                  SELECT 1
                  FROM ProjectMember pm
                  WHERE pm.project = pe.project
                    AND pm.student.user.email = :email
              )
            ORDER BY pe.createdAt DESC
            """)
    List<ProjectEvaluation> findByProjectOwnerOrMemberEmailOrderByCreatedAtDesc(@Param("email") String email);
}
