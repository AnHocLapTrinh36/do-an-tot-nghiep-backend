package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.AdminUpdateWithdrawalRequest;
import com.example.Backend.dto.request.CreateWithdrawalRequest;
import com.example.Backend.dto.response.WithdrawalResponse;
import com.example.Backend.service.WithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/withdrawals")
@RequiredArgsConstructor
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Sinh vien tao yeu cau rut tien")
    public ApiResponse<WithdrawalResponse> create(
            @Valid @RequestBody CreateWithdrawalRequest request,
            Authentication authentication
    ) {
        return ApiResponse.success(
                "Tao yeu cau rut tien thanh cong",
                withdrawalService.create(request, authentication.getName()),
                201
        );
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Sinh vien xem danh sach yeu cau rut tien cua minh")
    public ApiResponse<List<WithdrawalResponse>> getMyWithdrawals(Authentication authentication) {
        return ApiResponse.success(
                "Lay danh sach yeu cau rut tien thanh cong",
                withdrawalService.getMyWithdrawals(authentication.getName())
        );
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin xem tat ca yeu cau rut tien")
    public ApiResponse<List<WithdrawalResponse>> getAllWithdrawals() {
        return ApiResponse.success(
                "Lay danh sach yeu cau rut tien thanh cong",
                withdrawalService.getAllWithdrawals()
        );
    }

    @PatchMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin cap nhat trang thai yeu cau rut tien")
    public ApiResponse<WithdrawalResponse> updateByAdmin(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateWithdrawalRequest request,
            Authentication authentication
    ) {
        return ApiResponse.success(
                "Cap nhat yeu cau rut tien thanh cong",
                withdrawalService.updateByAdmin(id, request, authentication.getName())
        );
    }
}
