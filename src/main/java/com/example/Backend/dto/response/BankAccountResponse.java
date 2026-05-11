package com.example.Backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountResponse {
    private Long id;
    private String bankName;
    private String accountNumber;
    private LocalDateTime createdAt;
}
