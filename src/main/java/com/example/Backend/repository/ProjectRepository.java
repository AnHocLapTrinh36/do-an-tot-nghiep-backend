package com.example.Backend.repository;

import com.example.Backend.entity.Project;
import com.example.Backend.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p FROM Project p " +
            "WHERE p.status = 'APPROVED' " +
            "AND (:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:technologyId IS NULL OR EXISTS (SELECT 1 FROM p.technologies t WHERE t.id = :technologyId))")
    Page<Project> findPublicProjects(
            @Param("title") String title,
            @Param("technologyId") Long technologyId,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Project p
            WHERE p.student.user.email = :email
              AND p.status <> 'CLOSE'
            ORDER BY p.createdAt DESC
            """)
    List<Project> findByStudentEmailAndStatusNotClose(@Param("email") String email);

    @Query("""
            SELECT p
            FROM Project p
            WHERE p.course IS NOT NULL
              AND EXISTS (
                  SELECT 1
                  FROM p.course.teachers t
                  WHERE t.id = :teacherId
              )
            ORDER BY p.createdAt DESC
            """)
    List<Project> findProjectsForTeacherEvaluation(@Param("teacherId") Long teacherId);

    @Query("SELECT p FROM Project p " +
            "ORDER BY CASE WHEN p.status = 'PENDING' THEN 0 " +
            "              WHEN p.status = 'APPROVED' THEN 1 " +
            "              WHEN p.status = 'REJECTED' THEN 2 " +
            "              ELSE 3 END ASC, p.createdAt DESC")
    Page<Project> findAllProjectsForAdmin(Pageable pageable);

    List<Project> findTop10ByStatusOrderByViewCountDesc(ProjectStatus status);

    @Query("""
            SELECT DISTINCT p
            FROM Project p
            LEFT JOIN p.technologies t
            WHERE p.status = 'APPROVED'
              AND (:keyword IS NULL
                   OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.student.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY p.viewCount DESC, p.createdAt DESC
            """)
    List<Project> searchApprovedProjectsForAi(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT DISTINCT p
            FROM Project p
            LEFT JOIN FETCH p.technologies
            WHERE p.status = 'APPROVED'
            """)
    List<Project> findApprovedProjectsWithTechnologiesForAi();
}
