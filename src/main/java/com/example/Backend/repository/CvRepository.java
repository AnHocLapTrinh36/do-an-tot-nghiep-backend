package com.example.Backend.repository;

import com.example.Backend.entity.Cv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CvRepository  extends JpaRepository<Cv, Long> {
    /// lấy danh sách cv theo id của student
    List<Cv> findByStudentId(Long studentId);
    // Tìm CV của một sinh viên cụ thể
    Optional<Cv> findByIdAndStudentId(Long id, Long studentId);
    List<Cv>findAllByOrderByCreatedAtDesc();

    long countByStudentId(Long studentId); // dếm số cv của 1 sinh viên
}
