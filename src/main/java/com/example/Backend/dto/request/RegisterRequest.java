package com.example.Backend.dto.request;

import com.example.Backend.entity.RoleType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Size(min = 6, max = 100)
    private String password;
    @NotBlank
    @Size(min = 2, max = 100)
    @JsonProperty("full_name")
    private String fullName;
    @NotNull
    private RoleType role;
    // STUDENT, COMPANY CÓ THỂ REGISTER ĐƯỢC TỪ CLIENT, -> createdByAdmin false
    // ADMIN ĐƯỢC TỪ HỆ THỐNG , TEACHER THÌ CHỈ ADMIN MỚI TẠO DC -> createdByAdmin true
    private boolean createdByAdmin = false;
}
