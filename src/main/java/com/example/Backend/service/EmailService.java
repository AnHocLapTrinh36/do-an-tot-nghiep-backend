package com.example.Backend.service;

import com.example.Backend.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface EmailService {
    void sendVerificationEmail(User user);

}
