package com.example.Backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectPurchaseResponse {
    private Long id;
    private Long projectId;
    private String projectTitle;
    private String sourceCodeUrl;
    private LocalDateTime createdAt;
}
