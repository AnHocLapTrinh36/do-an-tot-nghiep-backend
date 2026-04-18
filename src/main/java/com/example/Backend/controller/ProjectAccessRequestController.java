package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.CreateProjectAccessRequestRequest;
import com.example.Backend.dto.request.admin.AdminUpdateProjectAccessRequestStatusRequest;
import com.example.Backend.dto.response.ProjectAccessRequestResponse;
import com.example.Backend.service.ProjectAccessRequestService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/project-access-requests")
@RequiredArgsConstructor
public class ProjectAccessRequestController {

    private final ProjectAccessRequestService projectAccessRequestService;

    @PostMapping
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "doanh nghiep gui yeu cau hop tac hoac truy cap project - role(company)")
    public ApiResponse<ProjectAccessRequestResponse> createProjectAccessRequest(
            @Valid @RequestBody CreateProjectAccessRequestRequest request,
            Authentication authentication
    ) {
        ProjectAccessRequestResponse response =
                projectAccessRequestService.createRequest(request, authentication.getName());
        return ApiResponse.success("Gui yeu cau hop tac thanh cong", response, 201);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "doanh nghiep xem danh sach yeu cau hop tac cua minh - role(company)")
    public ApiResponse<Page<ProjectAccessRequestResponse>> getMyProjectAccessRequests(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ProjectAccessRequestResponse> response =
                projectAccessRequestService.getMyRequests(authentication.getName(), page, size);
        return ApiResponse.success("Lay danh sach yeu cau hop tac thanh cong", response);
    }

    @GetMapping("/me/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "doanh nghiep xem chi tiet 1 yeu cau hop tac cua minh - role(company)")
    public ApiResponse<ProjectAccessRequestResponse> getMyProjectAccessRequestDetail(
            @PathVariable Long id,
            Authentication authentication
    ) {
        ProjectAccessRequestResponse response =
                projectAccessRequestService.getMyRequestDetail(id, authentication.getName());
        return ApiResponse.success("Lay chi tiet yeu cau hop tac thanh cong", response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "admin xem danh sach yeu cau hop tac project")
    public ApiResponse<Page<ProjectAccessRequestResponse>> getProjectAccessRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ProjectAccessRequestResponse> response = projectAccessRequestService.getAllRequests(page, size);
        return ApiResponse.success("Lay danh sach yeu cau hop tac thanh cong", response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "admin xem chi tiet yeu cau hop tac project")
    public ApiResponse<ProjectAccessRequestResponse> getProjectAccessRequestDetail(@PathVariable Long id) {
        ProjectAccessRequestResponse response = projectAccessRequestService.getRequestDetailForAdmin(id);
        return ApiResponse.success("Lay chi tiet yeu cau hop tac thanh cong", response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "admin duyet hoac tu choi yeu cau hop tac project")
    public ApiResponse<ProjectAccessRequestResponse> updateProjectAccessRequestStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateProjectAccessRequestStatusRequest request
    ) {
        ProjectAccessRequestResponse response = projectAccessRequestService.updateRequestStatus(id, request);
        return ApiResponse.success("Cap nhat trang thai yeu cau hop tac thanh cong", response);
    }
}
