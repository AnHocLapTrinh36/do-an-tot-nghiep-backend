package com.example.Backend.service;

import com.example.Backend.dto.request.ProjectEvaluationRequest;
import com.example.Backend.dto.request.ProjectEvaluationUpdateRequest;
import com.example.Backend.dto.response.ProjectEvaluationResponse;
import com.example.Backend.dto.response.TeacherProjectEvaluationItemResponse;

import java.util.List;

public interface ProjectEvaluationService {

    ProjectEvaluationResponse createEvaluation(ProjectEvaluationRequest request, String teacherEmail);

    ProjectEvaluationResponse updateEvaluation(Long evaluationId, ProjectEvaluationUpdateRequest request, String teacherEmail);

    List<TeacherProjectEvaluationItemResponse> getProjectsForEvaluation(String teacherEmail);

    ProjectEvaluationResponse getMyEvaluationByProject(Long projectId, String teacherEmail);

    List<ProjectEvaluationResponse> getEvaluationsForMyProjects(String studentEmail);

    List<ProjectEvaluationResponse> getEvaluationsByProject(Long projectId);
}
