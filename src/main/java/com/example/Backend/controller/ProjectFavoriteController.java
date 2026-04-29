package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.CreateProjectFavoriteRequest;
import com.example.Backend.dto.response.ProjectFavoriteResponse;
import com.example.Backend.service.ProjectFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project-favorites")
@RequiredArgsConstructor
public class ProjectFavoriteController {

    private final ProjectFavoriteService projectFavoriteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'COMPANY')")
    @Operation(summary = "luu project vao danh sach yeu thich - role(student, company)")
    public ApiResponse<ProjectFavoriteResponse> createFavorite(
            @Valid @RequestBody CreateProjectFavoriteRequest request,
            Authentication authentication
    ) {
        ProjectFavoriteResponse response = projectFavoriteService.createFavorite(request, authentication.getName());
        return ApiResponse.success("Luu project yeu thich thanh cong", response, 201);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('STUDENT', 'COMPANY')")
    @Operation(summary = "xem danh sach project yeu thich cua toi - role(student, company)")
    public ApiResponse<List<ProjectFavoriteResponse>> getMyFavorites(Authentication authentication) {
        return ApiResponse.success(
                "Lay danh sach project yeu thich thanh cong",
                projectFavoriteService.getMyFavorites(authentication.getName())
        );
    }

    @DeleteMapping("/projects/{projectId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'COMPANY')")
    @Operation(summary = "bo luu 1 project khoi danh sach yeu thich - role(student, company)")
    public ApiResponse<Void> unlikeProject(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        projectFavoriteService.unlikeProject(projectId, authentication.getName());
        return ApiResponse.success("Bo project khoi danh sach yeu thich thanh cong");
    }

    @GetMapping("/{projectId}/isFavorited")
    @PreAuthorize("hasAnyRole('STUDENT', 'COMPANY')")
    @Operation(summary = "Kiểm tra project đã được yêu thích chưa - role(student, company)")
    public ApiResponse<Boolean> isFavorited(
            @PathVariable Long projectId,
            Authentication authentication) {
        boolean status = projectFavoriteService.isProjectFavorited(projectId, authentication.getName());
        return ApiResponse.success("Lay trang thai yeu thich thanh cong", status);
    }
}
