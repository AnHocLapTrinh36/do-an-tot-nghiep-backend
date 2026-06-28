package com.example.Backend.repository;

import com.example.Backend.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    boolean existsByProjectIdAndStudentUserEmail(Long projectId, String email);
    boolean existsByProjectIdAndStudentId(Long projectId, Long studentId);
}
