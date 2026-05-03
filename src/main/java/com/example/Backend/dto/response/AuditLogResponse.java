package com.example.Backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long targetId;
    private String description;
    private LocalDateTime createdAt;
}
