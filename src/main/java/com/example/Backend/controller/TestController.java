package com.example.Backend.controller;

import com.example.Backend.entity.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    public String publicApi() {
        return "Endpoint công khai";
    }
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student")
    public String studentOnly() {
        return "Chỉ STUDENT mới vào được";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String adminOnly() {
        return "Chỉ ADMIN mới được truy cập";
    }

    @PreAuthorize("hasRole('COMPANY')")
    @GetMapping("/company")
    public String companyOnly() {
        return "Chỉ COMPANY mới được truy cập";
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/teacher")
    public String teacherOnly() {
        return "Chỉ TEACHER mới được truy cập";
    }

    @GetMapping("/me")
    public String currentUser(Authentication auth) {
        if (auth == null) return "Chưa đăng nhập";
        return "User: " + auth.getName() + " | Roles: " + auth.getAuthorities();
    }
}