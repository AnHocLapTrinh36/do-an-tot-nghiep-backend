package com.example.Backend.repository;

import com.example.Backend.entity.Project;
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

    List<Project> findByStudent_User_EmailOrderByCreatedAtDesc(String email);

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
}
