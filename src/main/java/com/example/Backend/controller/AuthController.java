package com.example.Backend.controller;

import com.example.Backend.dto.request.LoginRequest;
import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.RegisterRequest;
import com.example.Backend.dto.response.LoginResponse;
import com.example.Backend.dto.response.RegisterResponse;
import com.example.Backend.entity.User;
import com.example.Backend.entity.VerificationToken;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.repository.VerificationTokenRepository;
import com.example.Backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    /// Đăng ký dành cho student + company
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) throws BadRequestException {
        request.setCreatedByAdmin(false);
        RegisterResponse registeredUser = userService.register(request);
        ApiResponse<RegisterResponse> response = ApiResponse.success(
                "Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.",
                registeredUser,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    /// Đăng ký dành cho teacher + admin
    @PostMapping("/admin/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RegisterResponse>> adminRegister(@Valid @RequestBody RegisterRequest request) throws BadRequestException {
        request.setCreatedByAdmin(true);
        RegisterResponse registeredUser = userService.register(request);
        ApiResponse<RegisterResponse> response = ApiResponse.success(
                "Đăng ký thành công tài khoàn thành công - Role user:" + registeredUser.getRole(),
                registeredUser,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    /// login hệ thống
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) throws BadRequestException {
        LoginResponse loginResponse = userService.login(request);
        ApiResponse<LoginResponse> response = ApiResponse.success(
                "Đăng nhập thành công",
                loginResponse,
                200);
        return ResponseEntity.ok(response);
    }
    /// xác thực email
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        try {
            Optional<VerificationToken> optToken = tokenRepository.findByToken(token);
            //tim trong token trong db
            if (optToken.isEmpty()) {
                return ResponseEntity.badRequest().body("Token không hợp lệ.");
            }
            VerificationToken verificationToken = optToken.get();
            //check hiệu lực token
            if (verificationToken.isExpired()) {
                return ResponseEntity.badRequest().body("Token đã hết hạn.");
            }
            //lấy obj user
            User user = verificationToken.getUser(); //Entity

            user.setEmailVerified(true);
            userRepository.save(user);
            //xóa token sau khi user xác thực email
            tokenRepository.delete(verificationToken);
            return ResponseEntity.ok("Xác thực email thành công! Bạn có thể đăng nhập ngay.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Có lỗi xảy ra: " + e.getMessage());
        }
    }

    ///  @PostMapping("/forgot-password")
    /// nhận mail ->check->tạo mk mới trong db -> gửi mk mới qua mail cho user -> kết thúc

}
