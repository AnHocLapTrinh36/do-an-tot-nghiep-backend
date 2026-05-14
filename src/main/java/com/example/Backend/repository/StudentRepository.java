package com.example.Backend.repository;


import com.example.Backend.entity.Student;
import com.example.Backend.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId); ///tìm student thông qua id của user

    Optional<Student> findByUser_Email(String email); ///tìm student thông qua mail

    Optional<Student> findByMssv(String mssv); ///tìm sinh viên theo mssv

    boolean existsByMssv(String mssv); ///kiểm tra trong bảng student có tồn tại mssv

    Optional<Student> findByUser(User user); ///tìm student thông qua obj user
    // chức năng tự update profile
    Optional<Student> findByUserEmail(String email);

    @Query("""
            SELECT DISTINCT s
            FROM Student s
            LEFT JOIN s.cvs c
            WHERE (:keyword IS NULL
                   OR LOWER(s.mssv) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(s.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(s.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(s.course) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(c.contentJson, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:course IS NULL OR LOWER(s.course) LIKE LOWER(CONCAT('%', :course, '%')))
            ORDER BY s.createdAt DESC
            """)
    List<Student> searchStudentsForAi(
            @Param("keyword") String keyword,
            @Param("course") String course,
            Pageable pageable
    );
}
