package com.example.Backend.dto.request;

import com.example.Backend.entity.WithdrawalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateWithdrawalRequest {
    @NotNull(message = "Trang thai khong duoc de trong")
    private WithdrawalStatus status;

    private String adminNote;
}
