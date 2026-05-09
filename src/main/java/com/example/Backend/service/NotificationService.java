package com.example.Backend.service;

import com.example.Backend.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getMyNotifications(String email);

    void markAsRead(Long notificationId);

    void createNotification(String email, String type, String title, String content);
}
