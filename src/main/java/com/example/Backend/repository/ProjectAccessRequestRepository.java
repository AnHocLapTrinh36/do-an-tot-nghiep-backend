package com.example.Backend.repository;

import com.example.Backend.entity.ProjectAccessRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectAccessRequestRepository extends JpaRepository<ProjectAccessRequest, Long> {

    /// yêu cầu đã tồn tại
    boolean existsByCompany_IdAndProject_Id(Long companyId, Long projectId);
    /// danh sách yêu cầu của một company
    Page<ProjectAccessRequest> findByCompany_Id(Long companyId, Pageable pageable);
    /// tất cả yêu cầu hợp tác
    Page<ProjectAccessRequest> findAll(Pageable pageable);
}
