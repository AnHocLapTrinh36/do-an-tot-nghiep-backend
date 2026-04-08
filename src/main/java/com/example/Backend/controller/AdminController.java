package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.admin.AdminUpdateRequest;
import com.example.Backend.dto.response.AdminResponse;
import com.example.Backend.dto.response.JobResponse;
import com.example.Backend.service.AdminService;
import com.example.Backend.service.JobPostService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Chặn quyền toàn bộ class
public class AdminController {

    private final AdminService adminService;
    private final JobPostService jobPostService;

    @GetMapping("/me")
    @Operation(summary = "Xem thông tin cá nhân của Admin")
    public ResponseEntity<ApiResponse<AdminResponse>> getMyProfile() {
        AdminResponse response = adminService.getMyProfile();
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin Admin thành công", response));
    }

    @PutMapping("/me")
    @Operation(summary = "Cập nhật thông tin cá nhân của Admin")
    public ResponseEntity<ApiResponse<AdminResponse>> updateMyProfile(
            @RequestBody AdminUpdateRequest request) {
        AdminResponse response = adminService.updateMyProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ Admin thành công", response));
    }

    @GetMapping("/jobs/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách tin chờ duyệt -role(admin)")
    public ApiResponse<List<JobResponse>> getPendingJobs() {
        return ApiResponse.success("Thành công", jobPostService.getPendingJobsForAdmin());
    }

    @PatchMapping("/jobs/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Duyệt hoặc Từ chối tin - role(admin)")
    public ApiResponse<JobResponse> approveJob(
            @PathVariable Long id,
            @RequestParam String action,
            Authentication auth) {
        return ApiResponse.success("Thao tác thành công",
                jobPostService.approveJob(id, action, auth.getName()));
    }

    @PatchMapping("/jobs/{id}/close")
    @PreAuthorize("hasRole('COMPANY')or hasRole('ADMIN')")
    @Operation(summary = "Đóng tin tuyển dụng sớm - role(admin;company)")
    public ApiResponse<String> closeJob(@PathVariable Long id) {
        jobPostService.closeJob(id);
        return ApiResponse.success("Đã đóng tin tuyển dụng");
    }
}