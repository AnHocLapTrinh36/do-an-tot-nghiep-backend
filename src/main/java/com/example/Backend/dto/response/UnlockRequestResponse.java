package com.example.Backend.dto.response;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UnlockRequestResponse {
    private Long id; // ID của Ticket
    private Long cvId;
    private String cvTitle;
    private Long studentId;
    private String studentName;
    private String mssv;
    private String reason;
    private String status;
    private LocalDateTime requestedAt;
}
