package com.example.Backend.service.Implement;

import com.example.Backend.entity.User;
import com.example.Backend.entity.VerificationToken;
import com.example.Backend.repository.VerificationTokenRepository;
import com.example.Backend.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final VerificationTokenRepository tokenRepository;

    @Value("${app.backend.url}")
    private String backendUrl;

    @Override
    public void sendVerificationEmail(User user) {
        // Tạo token mới
        VerificationToken verificationToken = new VerificationToken(user);
        tokenRepository.save(verificationToken);

        // Tạo link verify
        String verificationUrl = backendUrl + "/api/auth/verify?token=" + verificationToken.getToken();

        // Nội dung email (có thể nâng cấp thành HTML sau)
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("hoaitam653@gmail.com");
        message.setTo(user.getEmail());
        message.setSubject("Xác thực tài khoản - Hệ thống Quản lý CV & Đồ án Khoa CNTT");
        message.setText(
                "Xin chào " + user.getFullName() + ",\n\n" +
                        "Cảm ơn bạn đã đăng ký tài khoản.\n" +
                        "Vui lòng click vào link dưới đây để xác thực email (link có hiệu lực trong 24 giờ):\n" +
                        verificationUrl + "\n\n" +
                        "Nếu bạn không đăng ký, vui lòng bỏ qua email này.\n\n" +
                        "Trân trọng,\n" +
                        "Khoa Công nghệ Thông tin"
        );
        try {
            mailSender.send(message);
            System.out.println("Gửi email xác thực thành công cho " + user.getEmail());
        } catch (Exception e) {
            // Log lỗi, không throw để không làm hỏng register
            System.err.println("Gửi email thất bại cho " + user.getEmail() + ": " + e.getMessage());
        }
    }
}
