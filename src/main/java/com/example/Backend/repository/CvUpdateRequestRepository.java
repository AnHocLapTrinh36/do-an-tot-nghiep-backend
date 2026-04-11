package com.example.Backend.repository;

import com.example.Backend.entity.CvUpdateRequest;
import com.example.Backend.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CvUpdateRequestRepository extends JpaRepository<CvUpdateRequest, Long> {
    // Kiểm tra xem có request nào đang chờ duyệt của CV này không để tránh spam
    boolean existsByCvIdAndStatus(Long cvId, Status status);

    Page<CvUpdateRequest> findByStatus(Status status, Pageable pageable);
}
