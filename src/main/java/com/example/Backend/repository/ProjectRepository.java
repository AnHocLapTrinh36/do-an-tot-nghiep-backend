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

    @Query(value = """
            SELECT p
            FROM Project p
            WHERE p.status = 'APPROVED'
              AND (:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))
              AND (
                  SELECT COUNT(DISTINCT t.id)
                  FROM Project p2
                  JOIN p2.technologies t
                  WHERE p2.id = p.id
                    AND t.id IN :technologyIds
              ) = :technologyCount
            ORDER BY p.createdAt DESC
            """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM Project p
                    WHERE p.status = 'APPROVED'
                      AND (:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))
                      AND (
                          SELECT COUNT(DISTINCT t.id)
                          FROM Project p2
                          JOIN p2.technologies t
                          WHERE p2.id = p.id
                            AND t.id IN :technologyIds
                      ) = :technologyCount
                    """)
    Page<Project> findPublicProjectsByTechnologyIds(
            @Param("title") String title,
            @Param("technologyIds") List<Long> technologyIds,
            @Param("technologyCount") long technologyCount,
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

    @Query(value = """
            SELECT p.*
            FROM projects p
            JOIN students s ON s.id = p.student_id
            JOIN users u ON u.id = s.user_id
            WHERE (:status IS NULL OR p.status = :status)
              AND (:studentName IS NULL OR u.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE CONCAT('%', :studentName, '%') COLLATE SQL_Latin1_General_CP1_CI_AI)
              AND (:mssv IS NULL OR s.mssv COLLATE SQL_Latin1_General_CP1_CI_AI LIKE CONCAT('%', :mssv, '%') COLLATE SQL_Latin1_General_CP1_CI_AI)
            ORDER BY CASE WHEN p.status = 'PENDING' THEN 0
                          WHEN p.status = 'APPROVED' THEN 1
                          WHEN p.status = 'REJECTED' THEN 2
                          ELSE 3 END ASC,
                     p.created_at DESC
            """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM projects p
                    JOIN students s ON s.id = p.student_id
                    JOIN users u ON u.id = s.user_id
                    WHERE (:status IS NULL OR p.status = :status)
                      AND (:studentName IS NULL OR u.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE CONCAT('%', :studentName, '%') COLLATE SQL_Latin1_General_CP1_CI_AI)
                      AND (:mssv IS NULL OR s.mssv COLLATE SQL_Latin1_General_CP1_CI_AI LIKE CONCAT('%', :mssv, '%') COLLATE SQL_Latin1_General_CP1_CI_AI)
                    """,
            nativeQuery = true)
    Page<Project> findAllProjectsForAdmin(
            @Param("studentName") String studentName,
            @Param("mssv") String mssv,
            @Param("status") String status,
            Pageable pageable
    );

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
