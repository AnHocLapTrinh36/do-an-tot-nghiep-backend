package com.example.Backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobApplicationResponse {
    private Long id;
    private String jobTitle;
    private String studentName;
    private String cvTitle;
    private String status;
    private LocalDateTime appliedAt;
}
