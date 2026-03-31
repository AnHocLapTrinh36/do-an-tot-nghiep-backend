package com.example.Backend.repository;


import com.example.Backend.entity.Student;
import com.example.Backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId); ///tìm student thông qua id của user
    Optional<Student> findByUser_Email(String email); ///tìm student thông qua mail
    Optional<Student> findByMssv(String mssv); ///tìm sinh viên theo mssv
    boolean existsByMssv(String mssv); ///kiểm tra trong bảng student có tồn tại mssv
    Optional<Student> findByUser(User user); ///tìm student thông qua obj user
    //chức năng tự update profile
    Optional<Student> findByUserEmail(String email);


}
