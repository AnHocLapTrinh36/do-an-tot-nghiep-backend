package com.example.Backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobFavoriteResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private String note;
    private LocalDateTime createdAt;
}
