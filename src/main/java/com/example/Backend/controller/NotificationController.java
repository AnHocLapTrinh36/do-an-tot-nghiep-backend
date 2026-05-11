package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.response.NotificationResponse;
import com.example.Backend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/me")
    @Operation(summary = "Lấy danh sách thông báo của người dùng hiện tại ")
    public ApiResponse<List<NotificationResponse>> getMyNotifications(Authentication authentication) {
        String email = authentication.getName();
        List<NotificationResponse> notifications = notificationService.getMyNotifications(email);
        return ApiResponse.success("Lấy danh sách thông báo thành công", notifications);
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Đánh dấu một thông báo là đã đọc")
    public ApiResponse<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ApiResponse.success("Đã đánh dấu thông báo là đã đọc", null);
    }
}
