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
    @Operation(summary = "lay danh sach cong ty - public")
    public ApiResponse<List<CompanyResponse>> getAll() {
        return ApiResponse.success("Lay danh sach cong ty thanh cong", companyService.getAllCompanies());
    }

    @GetMapping("/{id}")
    @Operation(summary = "lay thong tin chi tiet cong ty - public")
    public ApiResponse<CompanyResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("Lay thong tin cong ty thanh cong", companyService.getCompanyById(id));
    }

    @PreAuthorize("hasRole('COMPANY')")
    @PutMapping(value = "/me", consumes = "multipart/form-data")
    @Operation(summary = "cap nhat thong tin cong ty - role(company)")
    public ApiResponse<CompanyResponse> updateMyCompany(@ModelAttribute CompanyUpdateRequest request) {
        return ApiResponse.success("Cap nhat thong tin thanh cong", companyService.update(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "admin cap nhat trang thai cong ty - role(admin)")
    public ApiResponse<CompanyResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody AdminUpdateCompanyRequest request) {
        return ApiResponse.success("Cap nhat trang thai thanh cong", companyService.updateStatusByAdmin(id, request));
    }

    @Operation(summary = "admin xoa cong ty - role(admin)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        companyService.delete(id);
        return ApiResponse.success("Xoa cong ty thanh cong");
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "lay thong tin cong ty hien tai - role(company)")
    public ApiResponse<CompanyResponse> getMyCompany(Authentication authentication) {
        String email = authentication.getName();
        CompanyResponse company = companyService.getMyCompany(email);
        return ApiResponse.success("Lay thong tin cong ty thanh cong", company);
    }

    @PostMapping(value = "/jobs", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "doanh nghiep dang tin moi - role(company)")
    public ApiResponse<JobResponse> createJob(@ModelAttribute JobRequest request, Authentication auth) {
        return ApiResponse.success("Dang tin thanh cong, vui long cho admin duyet", jobPostService.createJob(request, auth.getName()));
    }

    @PutMapping(value = "/jobs/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "cap nhat tin tuyen dung - role(company)")
    public ApiResponse<JobResponse> updateJob(@PathVariable Long id, @ModelAttribute JobRequest request, Authentication auth) {
        return ApiResponse.success("Cap nhat thanh cong", jobPostService.updateJob(id, request, auth.getName()));
    }

    @PatchMapping("/jobs/{id}/close")
    @PreAuthorize("hasRole('COMPANY')or hasRole('ADMIN')")
    @Operation(summary = "dong tin tuyen dung som - role(admin;company)")
    public ApiResponse<String> closeJob(@PathVariable Long id) {
        jobPostService.closeJob(id);
        return ApiResponse.success("Da dong tin tuyen dung");
    }

    @GetMapping("/jobs/{id}/applications")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "lay danh sach cac don ung tuyen cua 1 bai viet - role(company)")
    public ResponseEntity<ApiResponse<List<ApplicantResponse>>> getJobApplications(@PathVariable Long id) {
        List<ApplicantResponse> applicants = jobApplicationService.getApplicantsByJob(id);
        return ResponseEntity.ok(ApiResponse.success("Lay danh sach ung vien thanh cong", applicants));
    }


    @GetMapping("/jobs/applications/approved")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "lấy danh sách các đơn ưngs tuyển đã được duyệt - role(company)")
    public ResponseEntity<ApiResponse<List<ApplicantResponse>>> getJobApplicationsApproved() {
        List<ApplicantResponse> applicants = jobApplicationService.getJobApplicationsApproved();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ứng viên thành công", applicants));
    }

    @GetMapping("/jobs/applications")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "lấy danh sách các đơn ưngs tuyển đã nộp - role(company)")
    public ResponseEntity<ApiResponse<List<ApplicantResponse>>> getJobApplications(Authentication auth) {
        List<ApplicantResponse> applicants = jobApplicationService.getApplicationByCompany(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ứng viên thành công", applicants));
    }

//    @GetMapping("/jobs/{id}/test")
//    @PreAuthorize("hasRole('COMPANY')")
//    @Operation(summary = "lấy danh sách các đơn ưngs tuyển của 1 bài viết - role(company)")
//    public ResponseEntity<ApiResponse<List<JobApplication>>> test(@PathVariable Long id) {
//        List<JobApplication> applicants = jobApplicationService.test(id);
//        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ứng viên thành công", applicants));
//    }


    @PatchMapping("/applications/{application_id}/status")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "doanh nghiep danh gia ho so ung tuyen - role(company)")
    public ResponseEntity<ApiResponse<JobApplicationResponse>> reviewApplication(
            @PathVariable("application_id") Long id,
            @RequestBody ApplicationReviewRequest request) {

        JobApplicationResponse response = jobApplicationService.reviewApplication(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cap nhat trang thai ung vien thanh cong", response));
    }

    @GetMapping("me/jobs")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "lay danh sach bai viet cua cong ty - role(company)")
    public ApiResponse<List<JobResponse>> getMyJobs(Authentication auth) {
        return ApiResponse.success(
                "Lay danh sach dang tin thanh cong",
                jobPostService.getJobsByCompanyEmail(auth.getName())
        );
    }
}
