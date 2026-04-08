package com.example.Backend.repository;


import com.example.Backend.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Integer Id);
}