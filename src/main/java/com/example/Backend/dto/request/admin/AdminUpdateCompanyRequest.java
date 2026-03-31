package com.example.Backend.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUpdateCompanyRequest {
    @NotBlank(message = "Trạng thái không được để trống")
    private String status;
    @NotNull(message = "ID Admin không được để trống")
    private Long adminId;
}
