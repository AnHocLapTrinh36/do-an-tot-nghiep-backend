package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.CompanyUpdateRequest;
import com.example.Backend.dto.request.admin.AdminUpdateCompanyRequest;
import com.example.Backend.dto.response.CompanyResponse;
import com.example.Backend.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping
    @Operation(summary = "lấy danh sách công ty - public ")
    public ApiResponse<List<CompanyResponse>> getAll() {
        return ApiResponse.success("Lấy danh sách công ty thành công", companyService.getAllCompanies());
    }

    @GetMapping("/{id}")
    @Operation(summary = "lấy thông tin chi tiết công ty - public")
    public ApiResponse<CompanyResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("Lấy thông tin công ty thành công", companyService.getCompanyById(id));
    }

    @PreAuthorize("hasRole('COMPANY')")
    @PutMapping(value = "/me", consumes = "multipart/form-data")
    @Operation(summary = "cập nhật thông tin công ty - role(company)")
    public ApiResponse<CompanyResponse> updateMyCompany(@ModelAttribute CompanyUpdateRequest request) {
        return ApiResponse.success("Cập nhật thông tin thành công", companyService.update(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = " admin Xác nhận, cập nhật trạng thái công ty - role(admin)")
    public ApiResponse<CompanyResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody AdminUpdateCompanyRequest request) {
        return ApiResponse.success("Cập nhật trạng thái thành công", companyService.updateStatusByAdmin(id, request));
    }

    @Operation(summary = "Admin xoá công ty - role(admin)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        companyService.delete(id);
        return ApiResponse.success("Xóa công ty thành công");
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "lấy thông tin công ty - role(company)")
    public ApiResponse<CompanyResponse> getMyCompany(Authentication authentication) {
        String email = authentication.getName();
        CompanyResponse company = companyService.getMyCompany(email);
        return ApiResponse.success("Lấy thông tin công ty thành công", company);
    }
}
