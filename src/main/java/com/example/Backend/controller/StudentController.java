package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.FavoritesRequest;
import com.example.Backend.dto.request.StudentUpdateRequest;
import com.example.Backend.dto.request.admin.AdminUpdateStudentRequest;
import com.example.Backend.dto.response.JobFavoriteResponse;
import com.example.Backend.dto.response.JobResponse;
import com.example.Backend.dto.response.StudentResponse;
import com.example.Backend.entity.JobFavorite;
import com.example.Backend.repository.JobFavoriteRepository;
import com.example.Backend.service.JobFavoriteService;
import com.example.Backend.service.JobPostService;
import com.example.Backend.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final JobPostService jobPostService;
    private final JobFavoriteService jobFavoriteService;
    private final JobFavoriteRepository jobFavoriteRepository;

    // Lấy tất cả sinh viên
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "danh sách sinh viên - admin")
    public ApiResponse<List<StudentResponse>> getAllStudents() {

        List<StudentResponse> students = studentService.getAllStudents();

        return ApiResponse.success("Lấy danh sách sinh viên thành công", students);
    }

    // Lấy chi tiết theo ID
    @GetMapping("/{id}")
    @Operation(summary = "chi tiết sinh viên - public")
    public ApiResponse<StudentResponse> getStudentById(@PathVariable Long id) {

        StudentResponse student = studentService.getStudentById(id);

        return ApiResponse.success("Lấy thông tin sinh viên thành công", student);
    }

    // Admin cập nhật sinh viên
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "chỉ cập nhật các trường cho phép trong phạm vi admin")
    public ApiResponse<StudentResponse> updateStudent(
            @PathVariable Long id,
            @RequestBody AdminUpdateStudentRequest request
    ) {
        StudentResponse updatedStudent = studentService.adminUpdateStudent(id, request);
        return ApiResponse.success("Cập nhật sinh viên thành công", updatedStudent);
    }

    // Xóa sinh viên
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "xóa sinh viên - admin")
    public ApiResponse<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ApiResponse.success("Xóa sinh viên thành công");
    }

    // Sinh viên tự cập nhật profile
    @PutMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Sinh viên tự cập nhật thông tin cá nhân")
    public ApiResponse<StudentResponse> updateMyProfile(
            @ModelAttribute StudentUpdateRequest request) throws IOException {   // Dùng @ModelAttribute vì có MultipartFile
        StudentResponse updated = studentService.updateMyProfile(request);
        return ApiResponse.success("Cập nhật thông tin cá nhân thành công", updated);
    }

    /// page profile
    /// api GET: sinh viên xem thông tin của mình (các trường của student + full_name)
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "lấy thông tin đầy đủ của student")
    public ApiResponse<StudentResponse> me(Authentication authentication) {
        String email = authentication.getName();
        StudentResponse student = studentService.getMyProfile(email);
        return ApiResponse.success("Lấy thông tin cá nhân thành công", student);
    }

    @PostMapping("/favorites/{jobId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Bật/Tắt trạng thái yêu thích tin tuyển dụng")
    public ApiResponse<Object> toggleFavorite(
            @PathVariable Long jobId,
            @RequestBody FavoritesRequest request,
            Authentication auth) {
        String note = request != null ? request.getNote() : null;
        Object result = jobFavoriteService.toggleFavorite(jobId, auth.getName(), note);
        if (result instanceof String) {
            return ApiResponse.success(result.toString(), null);
        }
        return ApiResponse.success("Đã thêm vào danh sách yêu thích", result);
    }

    @GetMapping("/favorites")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Hiển thị các tin đã yêu thích")
    public ApiResponse<List<JobFavoriteResponse>> getMyFavorites(Authentication auth) {
        return ApiResponse.success("Danh sách tin đã lưu", jobFavoriteService.getMyFavorites(auth.getName()));
    }
}