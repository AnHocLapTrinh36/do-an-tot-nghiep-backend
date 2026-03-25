package com.example.Backend.dto.Teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherResponse {
    private int id;
    private int userId;
    private String email;
    private String fullName;
    private String employeeCode;
}
