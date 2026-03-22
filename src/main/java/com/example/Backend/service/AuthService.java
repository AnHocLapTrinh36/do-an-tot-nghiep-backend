package com.example.Backend.service;

import com.example.Backend.dto.Auth.LoginRequest;
import com.example.Backend.dto.User.UserRequest;
import com.example.Backend.entity.RoleType;
import com.example.Backend.entity.User;
import com.example.Backend.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public User createUser(UserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Lỗi: Email này đã được sử dụng");
        }
        User newUser = new User();
        newUser.setRole(RoleType.STUDENT);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setEmail(request.getEmail());
        newUser.setIsActive(true);
        newUser.setFullName(request.getFullName());
        return userRepository.save(newUser);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new RuntimeException("Mật khẩu không chính xác");
        return jwtService.generateToken(user.getEmail());
    }
}
