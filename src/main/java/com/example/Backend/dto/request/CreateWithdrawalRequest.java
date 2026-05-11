package com.example.Backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateWithdrawalRequest {
    @NotNull(message = "So tien rut khong duoc de trong")
    @DecimalMin(value = "1000", message = "So tien rut toi thieu la 1000")
    private BigDecimal amount;

    @NotNull(message = "Tai khoan ngan hang khong duoc de trong")
    private Long bankAccountId;
}
