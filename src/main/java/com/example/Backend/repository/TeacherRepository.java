package com.example.Backend.repository;

import com.example.Backend.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByUserId(Long userId);

    boolean existsByEmployeeCode(String employeeCode);
}
