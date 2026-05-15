package com.example.Backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TeacherUpdateRequest {

    @NotBlank(message = "Tên giảng viên không được để trống")
    @Size(min = 3, max = 100, message = "Tên giảng viên phải từ 3 đến 100 ký tự")
    @JsonProperty("full_name")
    private String fullName;
}
