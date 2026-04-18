package com.example.Backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectFavoriteResponse {
    private Long id;

    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("project_title")
    private String projectTitle;

    @JsonProperty("student_name")
    private String studentName;

    private LocalDateTime createdAt;
}
