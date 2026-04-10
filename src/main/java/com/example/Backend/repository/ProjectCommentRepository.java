package com.example.Backend.repository;

import com.example.Backend.entity.ProjectComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectCommentRepository extends JpaRepository<ProjectComment, Long> {
    // Lấy danh sách comment theo project, sắp xếp mới nhất lên đầu
    List<ProjectComment> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}
