package com.example.Backend.service.Implement;

import com.example.Backend.dto.request.ProjectEvaluationRequest;
import com.example.Backend.dto.request.ProjectEvaluationUpdateRequest;
import com.example.Backend.dto.response.ProjectEvaluationResponse;
import com.example.Backend.entity.Project;
import com.example.Backend.entity.ProjectEvaluation;
import com.example.Backend.entity.ProjectStatus;
import com.example.Backend.entity.Teacher;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.repository.ProjectEvaluationRepository;
import com.example.Backend.repository.ProjectRepository;
import com.example.Backend.repository.TeacherRepository;
import com.example.Backend.service.ProjectEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectEvaluationServiceImpl implements ProjectEvaluationService {

    private final ProjectEvaluationRepository projectEvaluationRepository;
    private final ProjectRepository projectRepository;
    private final TeacherRepository teacherRepository;

    @Override
    @Transactional
    public ProjectEvaluationResponse createEvaluation(ProjectEvaluationRequest request, String teacherEmail) {
        Teacher teacher = teacherRepository.findByUser_Email(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đồ án"));

        if (projectEvaluationRepository.existsByProjectIdAndTeacherId(project.getId(), teacher.getId())) {
            throw new RuntimeException("Giảng viên đã đánh giá đồ án này rồi");
        }

        ProjectEvaluation evaluation = ProjectEvaluation.builder()
                .project(project)
                .teacher(teacher)
                .suggestions(request.getSuggestions().trim())
                .build();

        return toResponse(projectEvaluationRepository.save(evaluation));
    }

    @Override
    @Transactional
    public ProjectEvaluationResponse updateEvaluation(Long evaluationId, ProjectEvaluationUpdateRequest request, String teacherEmail) {
        Teacher teacher = teacherRepository.findByUser_Email(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));

        ProjectEvaluation evaluation = projectEvaluationRepository.findByIdAndTeacherId(evaluationId, teacher.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá của giảng viên"));

        if (evaluation.getProject().getStatus() == ProjectStatus.APPROVED) {
            throw new RuntimeException("Không thể chỉnh sửa đánh giá khi đồ án đã được phê duyệt");
        }

        evaluation.setSuggestions(request.getSuggestions().trim());
        return toResponse(projectEvaluationRepository.save(evaluation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectEvaluationResponse> getEvaluationsForMyProjects(String studentEmail) {
        return projectEvaluationRepository.findByProjectStudentUserEmailOrderByCreatedAtDesc(studentEmail)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectEvaluationResponse> getEvaluationsByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đồ án"));

        if (project.getStatus() != ProjectStatus.APPROVED) {
            throw new RuntimeException("Chỉ có thể xem đánh giá giảng viên khi đồ án đã được phê duyệt");
        }

        return projectEvaluationRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ProjectEvaluationResponse toResponse(ProjectEvaluation evaluation) {
        return ProjectEvaluationResponse.builder()
                .id(evaluation.getId())
                .projectId(evaluation.getProject().getId())
                .projectTitle(evaluation.getProject().getTitle())
                .teacherId(evaluation.getTeacher().getId())
                .teacherName(evaluation.getTeacher().getUser().getFullName())
                .suggestions(evaluation.getSuggestions())
                .createdAt(evaluation.getCreatedAt())
                .updatedAt(evaluation.getUpdatedAt())
                .build();
    }
}
