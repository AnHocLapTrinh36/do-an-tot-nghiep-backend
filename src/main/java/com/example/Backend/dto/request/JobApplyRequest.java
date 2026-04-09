package com.example.Backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobApplyRequest {
    @NotNull(message = "Vui lòng chọn CV để ứng tuyển")
    private Long cvId;
}
