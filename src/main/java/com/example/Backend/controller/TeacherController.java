package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.TeacherUpdateRequest;
import com.example.Backend.dto.response.TeacherResponse;
import com.example.Backend.entity.User;
import com.example.Backend.service.TeacherService;
import com.example.Backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {
    private final TeacherService teacherService;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách giảng viên - chỉ ADMIN mới được truy cập")
    public ResponseEntity<ApiResponse<List<TeacherResponse>>> getAll() {
        List<TeacherResponse> teachers = teacherService.getAllTeachers();
        ApiResponse<List<TeacherResponse>> response = ApiResponse.success(
                "Lấy danh sách giảng viên thành công",
                teachers);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Lấy thông tin giảng viên theo ID")
    public ResponseEntity<ApiResponse<TeacherResponse>> getById(@PathVariable Long id) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByEmail(currentEmail);
        TeacherResponse teacherResponse = teacherService.getTeacherById(id, currentUser);
        ApiResponse<TeacherResponse> response = ApiResponse.success(
                "Lấy thông tin giảng viên thành công",
                teacherResponse
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Giảng viên tự cập nhật tên của mình")
    public ResponseEntity<ApiResponse<TeacherResponse>> updateMyProfile(
            @Valid @RequestBody TeacherUpdateRequest request,
            Authentication authentication
    ) {
        TeacherResponse updatedTeacher = teacherService.updateMyProfile(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tên giảng viên thành công", updatedTeacher));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa giảng viên - chỉ ADMIN mới được thực hiện")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        ApiResponse<Void> response = ApiResponse.success("Xóa giảng viên thành công");
        return ResponseEntity.ok(response);
    }
}
