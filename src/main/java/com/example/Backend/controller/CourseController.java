package com.example.Backend.controller;


import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.entity.Course;

import com.example.Backend.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @GetMapping("/courses")
    public ApiResponse<List<Course>> getAll() {
        // Sử dụng ApiResponse.success(message, data)
        return ApiResponse.success("Lấy danh sách khóa học thành công", courseService.getAllCourses());
    }

    @GetMapping("/courses/{id}")
    public ApiResponse<Course> getById(@PathVariable Integer id) {
        // Nếu chỉ muốn trả về data mà không cần message riêng
        return ApiResponse.success("Lấy thông tin khóa học thành công", courseService.getCourseById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/courses")
    public ApiResponse<Course> create(@RequestBody Course course) {
        // Sử dụng ApiResponse.success(message, data, code) cho mã 201
        return ApiResponse.success("Tạo khóa học mới thành công", courseService.createCourse(course), 201);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/courses/{id}")
    public ApiResponse<Course> update(@PathVariable Integer id, @RequestBody Course course) {
        return ApiResponse.success("Cập nhật khóa học thành công", courseService.updateCourse(id, course));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/courses/{id}")
    public ApiResponse<String> delete(@PathVariable Integer id) {
        courseService.deleteCourse(id);
        return ApiResponse.success("Xóa khóa học thành công", "Đã xóa ID: " + id);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @PostMapping("/admin/courses/{courseId}/teachers/{teacherId}")
    public ApiResponse<Object> addTeacher(
            @PathVariable Integer courseId,
            @PathVariable Long teacherId) {

        Object result = courseService.addTeacherToCourse(courseId, teacherId);
        return ApiResponse.success("Đã phân công giáo viên vào khóa học thành công", result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/courses/{courseId}/teachers/{teacherId}")
    public ApiResponse<String> removeTeacher(
            @PathVariable Integer courseId,
            @PathVariable Long teacherId) {

        courseService.removeTeacherFromCourse(courseId, teacherId);
        return ApiResponse.success("Đã gỡ giáo viên khỏi khóa học thành công",
                "Course ID: " + courseId + " - Teacher ID: " + teacherId);
    }

}