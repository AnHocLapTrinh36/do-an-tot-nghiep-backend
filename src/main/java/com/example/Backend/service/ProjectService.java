package com.example.Backend.service;

import com.example.Backend.dto.request.ProjectRequest;
import com.example.Backend.dto.request.ProjectUpdateRequest;
import com.example.Backend.dto.request.admin.AdminApprovedProject;
import com.example.Backend.dto.response.ProjectResponse;
import com.example.Backend.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface ProjectService {
    ProjectResponse createProject(ProjectRequest request, String email);
    ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request, String email);
    void deleteProject(@PathVariable Long id);
    void updateProjectByAdmin(Long projectId, AdminApprovedProject request);
    Page<ProjectResponse> getPublicProjects(String title, List<Long> technologyIds, int page, int size);
    List<ProjectResponse> getMyProjects();
    Page<ProjectResponse> getAllProjectsForAdmin(String studentName, String mssv, ProjectStatus status, int page, int size);
    ProjectResponse getProjectDetail(Long id);




}
