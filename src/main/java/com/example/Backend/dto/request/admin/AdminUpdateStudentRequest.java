package com.example.Backend.dto.request.admin;


import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminUpdateStudentRequest {

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;

    // GPA thang 10
    @DecimalMin(value = "0.0", message = "GPA không được nhỏ hơn 0")
    @DecimalMax(value = "10.0", message = "GPA không được lớn hơn 10")
    private BigDecimal gpa;
}