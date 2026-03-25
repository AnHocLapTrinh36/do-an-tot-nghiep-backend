package com.example.Backend.dto.User;

import com.example.Backend.entity.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUserUpdateRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Size(min = 6, message = "Mật khẩu mới phải từ 6 ký tự")
    private String password;

    @NotBlank(message = "FullName không được để trống")
    private String fullName;

    @NotNull(message = "Không được để trống quyền")
    private RoleType role;

    private Boolean isActive;
}
