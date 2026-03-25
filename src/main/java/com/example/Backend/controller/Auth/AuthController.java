package com.example.Backend.controller.Auth;

import com.example.Backend.dto.Auth.LoginRequest;
import com.example.Backend.dto.ResponseApi.ApiResponse;
import com.example.Backend.dto.User.UserReponse;
import com.example.Backend.dto.User.UserRequest;
import com.example.Backend.entity.User;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ApiResponse register(@RequestBody UserRequest request) {
        UserReponse result = authService.createUser(request);
        return ApiResponse.builder()
                .code(201)
                .message("Đăng ký tài khoản thành công")
                .result(result)
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = authService.login(request);

        return ResponseEntity.ok(token);
    }
}
