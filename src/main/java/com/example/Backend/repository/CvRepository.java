package com.example.Backend.repository;

import com.example.Backend.entity.Cv;
import com.example.Backend.entity.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CvRepository extends JpaRepository<Cv, Long>, JpaSpecificationExecutor<Cv> {
    /// lấy danh sách cv theo id của student, bỏ qua CV đã xóa mềm
    List<Cv> findByStudentIdAndStatusNotOrderByCreatedAtDesc(Long studentId, Status status);

    Optional<Cv> findByIdAndStudentIdAndStatusNot(Long id, Long studentId, Status status);

    Optional<Cv> findByIdAndStatusNot(Long id, Status status);

    List<Cv> findAllByStatusNotOrderByCreatedAtDesc(Status status);

    long countByStudentIdAndStatusNot(Long studentId, Status status);

    @Query("""
            SELECT c
            FROM Cv c
            WHERE c.status = 'APPROVED'
              AND c.isPublic = true
            ORDER BY COALESCE(c.viewCount, 0) DESC, COALESCE(c.downloadCount, 0) DESC, c.createdAt DESC
            """)
    List<Cv> findTopPublicApprovedCvsByInterestForAi(Pageable pageable);

    List<Cv> findTop50ByStatusAndIsPublicTrueOrderByUpdatedAtDesc(Status status);
}
