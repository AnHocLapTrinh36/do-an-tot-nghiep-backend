package com.example.Backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApplicantResponse {
    private Long applicationId;
    private Long studentId;
    private String fullName;
    private String email;
    private String cvTitle;
    private String cvFileUrl;
    private String status;
    private LocalDateTime appliedAt;
}
