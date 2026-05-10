package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.response.OrderPaymentResponse;
import com.example.Backend.dto.response.PaymentLinkResponse;
import com.example.Backend.dto.response.ProjectPurchaseResponse;
import com.example.Backend.dto.response.WalletResponse;
import com.example.Backend.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.webhooks.Webhook;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/api/payments/projects/{projectId}/create-link")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'COMPANY', 'ADMIN')")
    @Operation(summary = "Tao link thanh toan payOS de mua source code")
    public ApiResponse<PaymentLinkResponse> createPaymentLink(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        return ApiResponse.success(
                "Tao link thanh toan thanh cong",
                paymentService.createPaymentLink(projectId, authentication.getName()),
                201
        );
    }

    @PostMapping("/payments/webhook")
    @Operation(summary = "Webhook payOS cap nhat trang thai thanh toan")
    public ApiResponse<Void> handlePayOSWebhook(@RequestBody Webhook webhook) {
        paymentService.handlePayOSWebhook(webhook);
        return ApiResponse.success("Xu ly webhook payOS thanh cong");
    }

//    @PostMapping("/api/payments/confirm-webhook")
//    @PreAuthorize("hasRole('ADMIN')")
//    @Operation(summary = "Admin khai bao webhook URL voi payOS")
//    public ApiResponse<String> confirmWebhook() {
//        return ApiResponse.success(
//                "Khai bao webhook payOS thanh cong",
//                paymentService.confirmWebhook()
//        );
//    }

    @GetMapping("/api/payments/orders/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem danh sach order thanh toan cua toi")
    public ApiResponse<List<OrderPaymentResponse>> getMyOrders(Authentication authentication) {
        return ApiResponse.success(
                "Lay danh sach order thanh cong",
                paymentService.getMyOrders(authentication.getName())
        );
    }

    @GetMapping("/api/payments/purchases/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem danh sach project da mua source code")
    public ApiResponse<List<ProjectPurchaseResponse>> getMyPurchases(Authentication authentication) {
        return ApiResponse.success(
                "Lay danh sach project da mua thanh cong",
                paymentService.getMyPurchases(authentication.getName())
        );
    }

    @GetMapping("/api/wallets/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Sinh vien xem so du vi cua minh")
    public ApiResponse<WalletResponse> getMyWallet(Authentication authentication) {
        return ApiResponse.success(
                "Lay thong tin vi thanh cong",
                paymentService.getMyWallet(authentication.getName())
        );
    }
}
