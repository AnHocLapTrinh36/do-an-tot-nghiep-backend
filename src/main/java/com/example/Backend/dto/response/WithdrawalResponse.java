package com.example.Backend.dto.response;

import com.example.Backend.entity.WithdrawalStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawalResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private BigDecimal amount;
    private WithdrawalStatus status;
    private String bankName;
    private String accountNumber;
    private String adminNote;
    private LocalDateTime requestedAt;
    private LocalDateTime updatedAt;
}
