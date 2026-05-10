package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.BankAccountRequest;
import com.example.Backend.dto.response.BankAccountResponse;
import com.example.Backend.service.BankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Sinh vien them tai khoan ngan hang de rut tien")
    public ApiResponse<BankAccountResponse> create(
            @Valid @RequestBody BankAccountRequest request,
            Authentication authentication
    ) {
        return ApiResponse.success(
                "Them tai khoan ngan hang thanh cong",
                bankAccountService.create(request, authentication.getName()),
                201
        );
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Sinh vien xem danh sach tai khoan ngan hang cua minh")
    public ApiResponse<List<BankAccountResponse>> getMyBankAccounts(Authentication authentication) {
        return ApiResponse.success(
                "Lay danh sach tai khoan ngan hang thanh cong",
                bankAccountService.getMyBankAccounts(authentication.getName())
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Sinh vien xoa tai khoan ngan hang")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication authentication) {
        bankAccountService.delete(id, authentication.getName());
        return ApiResponse.success("Xoa tai khoan ngan hang thanh cong");
    }
}
