package com.example.Backend.dto.Teacher;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherUpdateRequest {
    @Email(message = "Email không đúng định dạng!")
    private String email;

    @Size(min = 2, max = 50, message = "Tên phải từ 2 đến 50 ký tự!")
    private String fullName;

    private String employeeCode;

    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự!")
    private String password;
}
