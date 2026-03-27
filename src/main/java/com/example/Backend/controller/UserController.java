package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.ChangePasswordRequest;
import com.example.Backend.dto.response.LoginResponse;
import com.example.Backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    //PUT change-password
    /**
     * Đổi mật khẩu - User phải đang đăng nhập
     */
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) throws BadRequestException {
        /// authentication.getName() trả về giá trị của Subject trong JWT.
        String currentEmail = authentication.getName();   // email từ JWT
        userService.changePassword(currentEmail, request.getOldPassword(), request.getNewPassword());
        ApiResponse<String> response = ApiResponse.success("Đổi mật khẩu thành công");
        return ResponseEntity.ok(response);
    }
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponse>> getCurrentUser(Authentication authentication) {
        LoginResponse user = userService.getCurrentUser();
        log.info("User hiện tại: {}", user);
        return ResponseEntity.ok(
                ApiResponse.success("User hiện tại",user, 200)
        );
    }
}
