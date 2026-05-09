package com.example.Backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String type;
    private String title;
    private String content;
    private boolean isRead;
    private LocalDateTime sentAt;
}
