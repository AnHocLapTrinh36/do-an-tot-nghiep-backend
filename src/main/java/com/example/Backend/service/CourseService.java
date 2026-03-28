package com.example.Backend.service;


import com.example.Backend.entity.Course;

import java.util.List;

public interface CourseService {
    List<Course> getAllCourses();

    Course getCourseById(Integer id);

    Course createCourse(Course course);

    Course updateCourse(Integer id, Course course);

    void deleteCourse(Integer id);

    Course addTeacherToCourse(Integer courseId, Long teacherId);

    void removeTeacherFromCourse(Integer courseId, Long teacherId);
}