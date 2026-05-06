package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.ForgotPasswordRequest;
import com.example.Backend.dto.request.LoginRequest;
import com.example.Backend.dto.request.RegisterRequest;
import com.example.Backend.dto.response.LoginResponse;
import com.example.Backend.dto.response.RegisterResponse;
import com.example.Backend.entity.User;
import com.example.Backend.entity.VerificationToken;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.repository.VerificationTokenRepository;
import com.example.Backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký cho sinh viên và doanh nghiệp")
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

    @PostMapping("/admin/register")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin tạo tài khoản cho teacher hoặc admin")
    public ResponseEntity<ApiResponse<RegisterResponse>> adminRegister(
            @Valid @RequestBody RegisterRequest request) throws BadRequestException {
        request.setCreatedByAdmin(true);
        RegisterResponse registeredUser = userService.register(request);
        ApiResponse<RegisterResponse> response = ApiResponse.success(
                "Đăng ký tài khoản thành công - Role user: " + registeredUser.getRole(),
                registeredUser,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập hệ thống")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) throws BadRequestException {
        LoginResponse loginResponse = userService.login(request);
        ApiResponse<LoginResponse> response = ApiResponse.success(
                "Đăng nhập thành công",
                loginResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    @Operation(summary = "Xác thực mail qua link khi user click xác nhận")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        try {
            Optional<VerificationToken> optToken = tokenRepository.findByToken(token);
            if (optToken.isEmpty()) {
                return ResponseEntity.badRequest().body("Token không hợp lệ.");
            }

            VerificationToken verificationToken = optToken.get();
            if (verificationToken.isExpired()) {
                return ResponseEntity.badRequest().body("Token đã hết hạn.");
            }

            User user = verificationToken.getUser();
            user.setEmailVerified(true);
            userRepository.save(user);
            tokenRepository.delete(verificationToken);
            return ResponseEntity.ok("Xác thực email thành công! Bạn có thể đăng nhập ngay.");
        } catch (Exception exception) {
            return ResponseEntity.badRequest().body("Có lỗi xảy ra: " + exception.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Quên mật khẩu")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) throws BadRequestException {
        userService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Mật khẩu mới đã được gửi qua email."));
    }
}
