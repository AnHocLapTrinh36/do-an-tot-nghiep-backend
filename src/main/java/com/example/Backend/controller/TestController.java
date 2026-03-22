package com.example.Backend.controller;

import com.example.Backend.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@RestController = @Controller( đánh dấu là controller ) + @ResponseBody( báo hiệu trả về json)
@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/public")
    public String publicApi() {
        return "Public API - ai cũng vào được";
    }

    @GetMapping("/private")
    public String privateApi() {
        return "Private API - cần token";
    }

    @GetMapping("/me")
    public User getMe() {
        return (User) SecurityContextHolder//Mở ngăn kéo bảo mật của yêu cầu (Request) hiện tại ra.
                .getContext()
                .getAuthentication()//Lấy cái "Bộ hồ sơ xác thực" mà bạn đã dùng lệnh
                .getPrincipal();//: Lấy ra "Chủ thể" (Thông tin cốt lõi của người dùng).
    }
}