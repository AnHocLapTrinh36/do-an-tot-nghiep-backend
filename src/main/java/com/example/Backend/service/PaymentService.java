package com.example.Backend.service;

import com.example.Backend.dto.response.OrderPaymentResponse;
import com.example.Backend.dto.response.PaymentLinkResponse;
import com.example.Backend.dto.response.ProjectPurchaseResponse;
import com.example.Backend.dto.response.WalletResponse;
import vn.payos.model.webhooks.Webhook;

import java.util.List;

public interface PaymentService {
    PaymentLinkResponse createPaymentLink(Long projectId, String buyerEmail);

    void handlePayOSWebhook(Webhook webhook);

//    String confirmWebhook();

    List<OrderPaymentResponse> getMyOrders(String buyerEmail);

    List<ProjectPurchaseResponse> getMyPurchases(String buyerEmail);

    WalletResponse getMyWallet(String email);
}
