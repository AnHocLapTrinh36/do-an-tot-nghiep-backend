package com.example.Backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long id;
    private Long userId;
    private String fullName; // Lấy từ bảng User để hiển thị
    private String content;
    private LocalDateTime createdAt;
}
