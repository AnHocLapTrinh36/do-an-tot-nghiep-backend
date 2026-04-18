package com.example.Backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectEvaluationResponse {

    private Long id;

    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("project_title")
    private String projectTitle;

    @JsonProperty("teacher_id")
    private Long teacherId;

    @JsonProperty("teacher_name")
    private String teacherName;

    private String suggestions;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
