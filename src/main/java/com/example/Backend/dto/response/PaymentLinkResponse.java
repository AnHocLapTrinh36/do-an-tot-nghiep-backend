package com.example.Backend.dto.response;

import com.example.Backend.entity.OrderStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentLinkResponse {
    private Long orderId;
    private Long orderCode;
    private Long projectId;
    private BigDecimal price;
    private OrderStatus status;
    private String checkoutUrl;
}
