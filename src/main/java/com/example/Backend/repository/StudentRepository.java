package com.example.Backend.repository;


import com.example.Backend.entity.Student;
import com.example.Backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);

    Optional<Student> findByMssv(String mssv);

    boolean existsByMssv(String mssv);

    Optional<Student> findByUser(User user);

    //chức năng tự update profile
    Optional<Student> findByUserEmail(String email);


}
