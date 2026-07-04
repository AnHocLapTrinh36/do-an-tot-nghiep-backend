package com.example.Backend.service;

import com.example.Backend.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface EmailService {
    void sendVerificationEmail(User user);
    void sendForgotPasswordEmail(User user, String newPassword);
    void sendApplicationReviewEmail(User user, String companyName, String jobTitle, String status, String companyNote);

}
