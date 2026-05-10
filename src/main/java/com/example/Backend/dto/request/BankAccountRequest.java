package com.example.Backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountRequest {
    @NotBlank(message = "Ten ngan hang khong duoc de trong")
    private String bankName;

    @NotBlank(message = "So tai khoan khong duoc de trong")
    private String accountNumber;
}
