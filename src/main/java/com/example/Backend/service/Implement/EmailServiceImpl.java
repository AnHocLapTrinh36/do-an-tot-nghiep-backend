package com.example.Backend.service.Implement;

import com.example.Backend.entity.User;
import com.example.Backend.entity.VerificationToken;
import com.example.Backend.repository.VerificationTokenRepository;
import com.example.Backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private static final String SYSTEM_EMAIL = "hoaitam653@gmail.com";

    private final JavaMailSender mailSender;
    private final VerificationTokenRepository tokenRepository;

    @Value("${app.backend.url}")
    private String backendUrl;

    @Override
    public void sendVerificationEmail(User user) {
        VerificationToken verificationToken = new VerificationToken(user);
        tokenRepository.save(verificationToken);

        String verificationUrl = backendUrl + "/api/auth/verify?token=" + verificationToken.getToken();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(SYSTEM_EMAIL);
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
        } catch (Exception exception) {
            System.err.println("Gửi email thất bại cho " + user.getEmail() + ": " + exception.getMessage());
        }
    }

    @Override
    public void sendForgotPasswordEmail(User user, String newPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(SYSTEM_EMAIL);
        message.setTo(user.getEmail());
        message.setSubject("Mật khẩu mới - Hệ thống Quản lý CV & Đồ án Khoa CNTT");
        message.setText(
                "Xin chào " + user.getFullName() + ",\n\n" +
                        "Hệ thống đã tạo mật khẩu mới cho tài khoản của bạn.\n" +
                        "Mật khẩu tạm thời: " + newPassword + "\n\n" +
                        "Vui lòng đăng nhập và đổi mật khẩu ngay sau khi vào hệ thống để đảm bảo an toàn.\n\n" +
                        "Trân trọng,\n" +
                        "Khoa Công nghệ Thông tin"
        );

        mailSender.send(message);
    }

    @Override
    public void sendApplicationReviewEmail(User user, String companyName, String jobTitle, String status, String companyNote) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(SYSTEM_EMAIL);
        message.setTo(user.getEmail());

        String subject;
        String statusText;
        if ("APPROVED".equalsIgnoreCase(status)) {
            subject = "Hồ sơ ứng tuyển đã được chấp nhận";
            statusText = "đã được chấp nhận";
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            subject = "Cập nhật kết quả ứng tuyển";
            statusText = "không được chọn trong đợt này";
        } else {
            subject = "Cập nhật trạng thái hồ sơ ứng tuyển";
            statusText = "đã được cập nhật";
        }

        String noteText = (companyNote == null || companyNote.isBlank())
                ? "Doanh nghiệp chưa để lại ghi chú thêm."
                : companyNote;

        message.setSubject(subject + " - Hệ thống Quản lý CV & Đồ án Khoa CNTT");
        message.setText(
                "Xin chào " + user.getFullName() + ",\n\n" +
                        "Hồ sơ ứng tuyển của bạn cho vị trí \"" + jobTitle + "\" tại công ty \"" + companyName + "\" " + statusText + ".\n\n" +
                        "Ghi chú từ doanh nghiệp: " + noteText + "\n\n" +
                        "Bạn có thể đăng nhập vào hệ thống để xem chi tiết trạng thái hồ sơ.\n\n" +
                        "Trân trọng,\n" +
                        "Khoa Công nghệ Thông tin"
        );

        try {
            mailSender.send(message);
            System.out.println("Gửi email phản hồi ứng tuyển thành công cho " + user.getEmail());
        } catch (Exception exception) {
            System.err.println("Gửi email phản hồi ứng tuyển thất bại cho " + user.getEmail() + ": " + exception.getMessage());
        }
    }
}
