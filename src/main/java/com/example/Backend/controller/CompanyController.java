package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.CompanyUpdateRequest;
import com.example.Backend.dto.request.admin.AdminUpdateCompanyRequest;
import com.example.Backend.dto.response.CompanyResponse;
import com.example.Backend.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping
    public ApiResponse<List<CompanyResponse>> getAll() {
        return ApiResponse.success("Lấy danh sách công ty thành công", companyService.getAllCompanies());
    }

    @GetMapping("/{id}")
    public ApiResponse<CompanyResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("Lấy thông tin công ty thành công", companyService.getCompanyById(id));
    }

    @PreAuthorize("hasRole('COMPANY')")
    @PutMapping(value = "/me", consumes = "multipart/form-data")
    public ApiResponse<CompanyResponse> updateMyCompany(@ModelAttribute CompanyUpdateRequest request) {
        return ApiResponse.success("Cập nhật thông tin thành công", companyService.update(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<CompanyResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody AdminUpdateCompanyRequest request) {
        return ApiResponse.success("Cập nhật trạng thái thành công", companyService.updateStatusByAdmin(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        companyService.delete(id);
        return ApiResponse.success("Xóa công ty thành công");
    }
}
