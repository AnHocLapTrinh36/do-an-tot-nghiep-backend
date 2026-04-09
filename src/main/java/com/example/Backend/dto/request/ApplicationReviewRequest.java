package com.example.Backend.dto.request;

import com.example.Backend.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApplicationReviewRequest {
    @NotNull(message = "Trạng thái không được để trống")
    private ApplicationStatus status;

    @Size(max = 500, message = "Ghi chú không quá 500 ký tự")
    private String companyNote;
}
