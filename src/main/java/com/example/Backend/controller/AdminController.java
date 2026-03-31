package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.admin.AdminUpdateRequest;
import com.example.Backend.dto.response.AdminResponse;
import com.example.Backend.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Chặn quyền toàn bộ class
public class AdminController {

    private final AdminService adminService;

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
}