package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.ProjectRequest;
import com.example.Backend.dto.request.ProjectUpdateRequest;
import com.example.Backend.dto.request.admin.AdminApprovedProject;
import com.example.Backend.dto.response.ProjectResponse;
import com.example.Backend.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Sinh viên tạo/nộp đồ án mới")
    public ApiResponse<ProjectResponse> createProject(
            @ModelAttribute ProjectRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        ProjectResponse createdProject = projectService.createProject(request, email);
        return ApiResponse.success("Tải lên đồ án thành công", createdProject, 201);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Sinh viên cập nhật đồ án của mình")
    public ApiResponse<ProjectResponse> updateProject(
            @PathVariable Long id,
            @ModelAttribute ProjectUpdateRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        ProjectResponse updatedProject = projectService.updateProject(id, request, email);

        return ApiResponse.success("Cập nhật đồ án thành công. Trạng thái đã chuyển về chờ duyệt.", updatedProject);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Xóa đồ án (Chủ sở hữu hoặc Admin)")
    public ApiResponse<Void> deleteProject(@PathVariable Long id) {

        // Chỉ cần gọi service và truyền ID
        projectService.deleteProject(id);

        return ApiResponse.success("Đồ án đã được đóng (Xóa thành công)");
    }
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin duyệt hoặc từ chối đồ án")
    public ApiResponse<Void> updateProjectStatus(
            @PathVariable Long id,
            @RequestBody AdminApprovedProject request
    ) {
        // Validate dữ liệu gửi lên
        if (request.getStatus() == null) {
            return ApiResponse.error("Trạng thái (status) không được để trống", 400);
        }

        projectService.updateProjectByAdmin(id, request);

        String message = request.getStatus().name().equals("APPROVED")
                ? "Đã duyệt đồ án thành công."
                : "Đã từ chối đồ án và lưu lại ghi chú.";
        return ApiResponse.success(message);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách đồ án (Trang chủ Public)")
    public ApiResponse<Page<ProjectResponse>> getPublicProjects(
            @RequestParam(required = false, name = "title") String title,
            @RequestParam(required = false, name = "technologyId") Long technologyId,
            @RequestParam(defaultValue = "0", name = "page") int page,
            @RequestParam(defaultValue = "10", name = "size") int size
    ) {

        Page<ProjectResponse> result = projectService.getPublicProjects(title, technologyId, page, size);

        return ApiResponse.success("Lấy danh sách đồ án thành công", result);
    }

    @GetMapping("/me/projects")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Sinh viên xem toàn bộ danh sách đồ án cá nhân")
    public ApiResponse<List<ProjectResponse>> getMyProjects() {

        List<ProjectResponse> myProjects = projectService.getMyProjects();

        return ApiResponse.success("Lấy danh sách đồ án cá nhân thành công", myProjects);
    }
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin lấy toàn bộ đồ án (Ưu tiên PENDING lên đầu)")
    public ApiResponse<Page<ProjectResponse>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ProjectResponse> result = projectService.getAllProjectsForAdmin(page, size);
        return ApiResponse.success("Lấy danh sách đồ án cho Admin thành công", result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết đồ án (Public/Owner/Admin)")
    public ApiResponse<ProjectResponse> getProjectDetail(@PathVariable Long id) {
        ProjectResponse project = projectService.getProjectDetail(id);
        return ApiResponse.success("Lấy chi tiết đồ án thành công", project);
    }

}