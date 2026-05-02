package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.ProjectEvaluationRequest;
import com.example.Backend.dto.request.ProjectEvaluationUpdateRequest;
import com.example.Backend.dto.response.ProjectEvaluationResponse;
import com.example.Backend.dto.response.TeacherProjectEvaluationItemResponse;
import com.example.Backend.service.ProjectEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProjectEvaluationController {

    private final ProjectEvaluationService projectEvaluationService;

    @PostMapping("/api/teacher/project-evaluations")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Giảng viên tạo đánh giá cho đồ án")
    public ApiResponse<ProjectEvaluationResponse> createEvaluation(
            @Valid @RequestBody ProjectEvaluationRequest request,
            Authentication authentication
    ) {
        ProjectEvaluationResponse response = projectEvaluationService.createEvaluation(request, authentication.getName());
        return ApiResponse.success("Tạo đánh giá đồ án thành công", response, 201);
    }

    @PutMapping("/api/teacher/project-evaluations/{evaluationId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Giảng viên cập nhật đánh giá của mình khi đồ án chưa được phê duyệt")
    public ApiResponse<ProjectEvaluationResponse> updateEvaluation(
            @PathVariable Long evaluationId,
            @Valid @RequestBody ProjectEvaluationUpdateRequest request,
            Authentication authentication
    ) {
        ProjectEvaluationResponse response = projectEvaluationService.updateEvaluation(evaluationId, request, authentication.getName());
        return ApiResponse.success("Cập nhật đánh giá đồ án thành công", response);
    }

    @GetMapping("/api/teacher/projects-for-evaluation")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Giảng viên xem danh sách đồ án thuộc môn học mình phụ trách để đánh giá")
    public ApiResponse<List<TeacherProjectEvaluationItemResponse>> getProjectsForEvaluation(Authentication authentication) {
        List<TeacherProjectEvaluationItemResponse> responses =
                projectEvaluationService.getProjectsForEvaluation(authentication.getName());
        return ApiResponse.success("Lấy danh sách đồ án để đánh giá thành công", responses);
    }

    @GetMapping("/api/teacher/projects/{projectId}/my-evaluation")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Giảng viên xem chi tiết đánh giá của mình theo project để sửa")
    public ApiResponse<ProjectEvaluationResponse> getMyEvaluationByProject(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        ProjectEvaluationResponse response =
                projectEvaluationService.getMyEvaluationByProject(projectId, authentication.getName());
        return ApiResponse.success("Lấy chi tiết đánh giá thành công", response);
    }

    @GetMapping("/api/students/me/project-evaluations")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Sinh viên xem tất cả đánh giá giảng viên trên các đồ án của mình")
    public ApiResponse<List<ProjectEvaluationResponse>> getEvaluationsForMyProjects(Authentication authentication) {
        List<ProjectEvaluationResponse> responses = projectEvaluationService.getEvaluationsForMyProjects(authentication.getName());
        return ApiResponse.success("Lấy danh sách đánh giá đồ án thành công", responses);
    }

    @GetMapping("/api/projects/{projectId}/teacher-evaluations")
    @Operation(summary = "Lấy tất cả đánh giá của giảng viên cho một đồ án cụ thể")
    public ApiResponse<List<ProjectEvaluationResponse>> getEvaluationsByProject(@PathVariable Long projectId) {
        List<ProjectEvaluationResponse> responses = projectEvaluationService.getEvaluationsByProject(projectId);
        return ApiResponse.success("Lấy danh sách đánh giá giảng viên thành công", responses);
    }
}
