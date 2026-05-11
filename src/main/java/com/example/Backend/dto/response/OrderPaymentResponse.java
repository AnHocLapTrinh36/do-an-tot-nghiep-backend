package com.example.Backend.dto.response;

import com.example.Backend.entity.OrderStatus;
import com.example.Backend.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPaymentResponse {
    private Long orderId;
    private Long orderCode;
    private Long projectId;
    private String projectTitle;
    private BigDecimal price;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
