package com.example.Backend.service.Implement;

import com.example.Backend.dto.request.ProjectEvaluationRequest;
import com.example.Backend.dto.request.ProjectEvaluationUpdateRequest;
import com.example.Backend.dto.response.ProjectEvaluationResponse;
import com.example.Backend.dto.response.TeacherProjectEvaluationItemResponse;
import com.example.Backend.entity.Project;
import com.example.Backend.entity.ProjectEvaluation;
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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        validateTeacherCanEvaluateProject(teacher, project);

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

        validateTeacherCanEvaluateProject(teacher, evaluation.getProject());

        evaluation.setSuggestions(request.getSuggestions().trim());
        return toResponse(projectEvaluationRepository.save(evaluation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherProjectEvaluationItemResponse> getProjectsForEvaluation(String teacherEmail) {
        Teacher teacher = teacherRepository.findByUser_Email(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));

        List<Project> projects = projectRepository.findProjectsForTeacherEvaluation(teacher.getId());
        Map<Long, ProjectEvaluation> evaluationMap = projectEvaluationRepository.findByTeacherId(teacher.getId())
                .stream()
                .collect(Collectors.toMap(
                        evaluation -> evaluation.getProject().getId(),
                        Function.identity(),
                        (first, second) -> first
                ));

        return projects.stream()
                .map(project -> {
                    ProjectEvaluation evaluation = evaluationMap.get(project.getId());
                    return TeacherProjectEvaluationItemResponse.builder()
                            .projectId(project.getId())
                            .projectTitle(project.getTitle())
                            .studentName(project.getStudent().getUser().getFullName())
                            .courseName(project.getCourse() != null ? project.getCourse().getName() : null)
                            .status(project.getStatus())
                            .hasEvaluation(evaluation != null)
                            .evaluationId(evaluation != null ? evaluation.getId() : null)
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectEvaluationResponse getMyEvaluationByProject(Long projectId, String teacherEmail) {
        Teacher teacher = teacherRepository.findByUser_Email(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đồ án"));

        validateTeacherCanEvaluateProject(teacher, project);

        ProjectEvaluation evaluation = projectEvaluationRepository.findByProjectIdAndTeacherId(projectId, teacher.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Giảng viên chưa có đánh giá cho đồ án này"));

        return toResponse(evaluation);
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

        return projectEvaluationRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateTeacherCanEvaluateProject(Teacher teacher, Project project) {
        if (project.getCourse() == null) {
            throw new RuntimeException("Đồ án chưa được gán môn học để giảng viên đánh giá");
        }

        boolean isAssignedTeacher = project.getCourse().getTeachers().stream()
                .anyMatch(courseTeacher -> courseTeacher.getId().equals(teacher.getId()));

        if (!isAssignedTeacher) {
            throw new RuntimeException("Giảng viên không được phép đánh giá đồ án ngoài môn học mình phụ trách");
        }
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
