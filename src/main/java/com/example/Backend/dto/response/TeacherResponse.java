package com.example.Backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherResponse {
    private Long id;
    @JsonProperty("user_id")
    private int userId;
    private String email;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("employee_code")
    private String employeeCode;
}
