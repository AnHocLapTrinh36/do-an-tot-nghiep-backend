package com.example.Backend.dto.request;

import com.example.Backend.entity.RoleType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import javax.management.relation.Role;


@Data
public class LoginRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu  phải từ 6 ký tự trở lên")
    private String password;
    @Enumerated(EnumType.STRING)
    private RoleType role;
}
