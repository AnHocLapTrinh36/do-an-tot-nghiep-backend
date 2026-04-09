package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.ApplicationReviewRequest;
import com.example.Backend.dto.request.CompanyUpdateRequest;
import com.example.Backend.dto.request.JobRequest;
import com.example.Backend.dto.request.admin.AdminUpdateCompanyRequest;
import com.example.Backend.dto.response.ApplicantResponse;
import com.example.Backend.dto.response.CompanyResponse;
import com.example.Backend.dto.response.JobApplicationResponse;
import com.example.Backend.dto.response.JobResponse;
import com.example.Backend.entity.JobApplication;
import com.example.Backend.service.CompanyService;
import com.example.Backend.service.JobApplicationService;
import com.example.Backend.service.JobPostService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;
    private final JobPostService jobPostService;
    private final JobApplicationService jobApplicationService;

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

    @PostMapping(value = "/jobs", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Doanh nghiệp đăng tin mới - role(company)")
    public ApiResponse<JobResponse> createJob(@ModelAttribute JobRequest request, Authentication auth) {
        return ApiResponse.success("Đăng tin thành công, vui lòng chờ admin duyệt", jobPostService.createJob(request, auth.getName()));
    }

    @PutMapping(value = "/jobs/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Cập nhật tin tuyển dụng - role(company)")
    public ApiResponse<JobResponse> updateJob(@PathVariable Long id, @ModelAttribute JobRequest request, Authentication auth) {

        return ApiResponse.success("Cập nhật thành công", jobPostService.updateJob(id, request, auth.getName()));
    }

    @PatchMapping("/jobs/{id}/close")
    @PreAuthorize("hasRole('COMPANY')or hasRole('ADMIN')")
    @Operation(summary = "Đóng tin tuyển dụng sớm - role(admin;company)")
    public ApiResponse<String> closeJob(@PathVariable Long id) {
        jobPostService.closeJob(id);
        return ApiResponse.success("Đã đóng tin tuyển dụng");
    }

    @GetMapping("/jobs/{id}/applications")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "lấy danh sách các đơn ưngs tuyển của 1 bài viết - role(company)")
    public ResponseEntity<ApiResponse<List<ApplicantResponse>>> getJobApplications(@PathVariable Long id) {
        List<ApplicantResponse> applicants = jobApplicationService.getApplicantsByJob(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ứng viên thành công", applicants));
    }

    @GetMapping("/jobs/{id}/test")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "lấy danh sách các đơn ưngs tuyển của 1 bài viết - role(company)")
    public ResponseEntity<ApiResponse<List<JobApplication>>> test(@PathVariable Long id) {
        List<JobApplication> applicants = jobApplicationService.test(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ứng viên thành công", applicants));
    }

    @PatchMapping("/applications/{application_id}/status")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Doanh nghiệp đánh giá hồ sơ ứng tuyển - role(company)")
    public ResponseEntity<ApiResponse<JobApplicationResponse>> reviewApplication(
            @PathVariable("application_id") Long id,
            @RequestBody ApplicationReviewRequest request) {

        // Gọi service xử lý
        JobApplicationResponse response = jobApplicationService.reviewApplication(id, request);

        // Trả về bọc trong ApiResponse như các API khác của bạn
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái ứng viên thành công", response));
    }
}
