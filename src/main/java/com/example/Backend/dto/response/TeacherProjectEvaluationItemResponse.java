package com.example.Backend.dto.response;

import com.example.Backend.entity.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherProjectEvaluationItemResponse {

    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("project_title")
    private String projectTitle;

    @JsonProperty("student_name")
    private String studentName;

    @JsonProperty("course_name")
    private String courseName;

    private ProjectStatus status;

    @JsonProperty("has_evaluation")
    private Boolean hasEvaluation;

    @JsonProperty("evaluation_id")
    private Long evaluationId;
}
