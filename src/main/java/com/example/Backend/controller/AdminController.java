package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.admin.AdminUpdateRequest;
import com.example.Backend.dto.response.AdminResponse;
import com.example.Backend.dto.response.AuditLogResponse;
import com.example.Backend.dto.response.JobResponse;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.AdminService;
import com.example.Backend.service.JobPostService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final JobPostService jobPostService;
    private final AuditLogService auditLogService;

    @GetMapping("/me")
    @Operation(summary = "xem thong tin ca nhan cua admin")
    public ResponseEntity<ApiResponse<AdminResponse>> getMyProfile() {
        AdminResponse response = adminService.getMyProfile();
        return ResponseEntity.ok(ApiResponse.success("Lay thong tin admin thanh cong", response));
    }

    @PutMapping("/me")
    @Operation(summary = "cap nhat thong tin ca nhan cua admin")
    public ResponseEntity<ApiResponse<AdminResponse>> updateMyProfile(
            @RequestBody AdminUpdateRequest request) {
        AdminResponse response = adminService.updateMyProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Cap nhat ho so admin thanh cong", response));
    }

    @GetMapping("/jobs/pending")
    @Operation(summary = "lay danh sach tin cho duyet - role(admin)")
    public ApiResponse<List<JobResponse>> getPendingJobs() {
        return ApiResponse.success("Thanh cong", jobPostService.getPendingJobsForAdmin());
    }

    @PatchMapping("/jobs/{id}/approve")
    @Operation(summary = "duyet hoac tu choi tin - role(admin)")
    public ApiResponse<JobResponse> approveJob(
            @PathVariable Long id,
            @RequestParam String action,
            Authentication auth) {
        return ApiResponse.success("Thao tac thanh cong",
                jobPostService.approveJob(id, action, auth.getName()));
    }

    @PatchMapping("/jobs/{id}/close")
    @PreAuthorize("hasRole('COMPANY')or hasRole('ADMIN')")
    @Operation(summary = "dong tin tuyen dung som - role(admin;company)")
    public ApiResponse<String> closeJob(@PathVariable Long id) {
        jobPostService.closeJob(id);
        return ApiResponse.success("Da dong tin tuyen dung");
    }

    @GetMapping("/logs")
    @Operation(summary = "lấy danh sách log thao tác của admin")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AuditLogResponse> response = auditLogService.getAuditLogs(page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách log thành công", response));
    }
}
