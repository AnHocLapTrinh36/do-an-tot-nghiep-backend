package com.example.Backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectEvaluationRequest {

    @NotNull(message = "projectId không được để trống")
    private Long projectId;

    @NotBlank(message = "Nội dung đánh giá không được để trống")
    private String suggestions;
}
