package com.example.Backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectEvaluationUpdateRequest {

    @NotBlank(message = "Nội dung đánh giá không được để trống")
    private String suggestions;
}
