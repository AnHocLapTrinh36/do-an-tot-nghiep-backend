package com.example.Backend.service.Implement;

import com.example.Backend.dto.response.NotificationResponse;
import com.example.Backend.entity.Notification;
import com.example.Backend.entity.User;
import com.example.Backend.mapper.NotificationMapper;
import com.example.Backend.repository.NotificationRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;//dùng cho websocket

    @Override
    public List<NotificationResponse> getMyNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderBySentAtDesc(user.getId());
        return notificationMapper.toResponseList(notifications);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));
        notification.setRead(true);
        // Không cần gọi save() vì có @Transactional, Hibernate sẽ tự cập nhật khi kết thúc method
    }

    @Override
    @Transactional
    public void createNotification(String email, String type, String title, String content) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người nhận không tồn tại: " + email));

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .content(content)
                .isRead(false)
                .build();
        String destination = "/topic/notifications/" + notification.getUser().getEmail();
        System.out.println("email cuar nguoi dung la:" + notification.getUser().getEmail());
        messagingTemplate.convertAndSend(destination, "Bạn vừa nhận được thông báo mới");
        notificationRepository.save(notification);
    }
}
