package com.example.Backend.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponse {
    private Long id;
    private BigDecimal totalBalance;
    private BigDecimal availableBalance;
    private LocalDateTime updatedAt;
}
