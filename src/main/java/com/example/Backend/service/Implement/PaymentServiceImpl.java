package com.example.Backend.service.Implement;

import com.example.Backend.dto.response.OrderPaymentResponse;
import com.example.Backend.dto.response.PaymentLinkResponse;
import com.example.Backend.dto.response.ProjectPurchaseResponse;
import com.example.Backend.dto.response.WalletResponse;
import com.example.Backend.entity.*;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.repository.*;
import com.example.Backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PayOS payOS;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ProjectPurchaseRepository projectPurchaseRepository;
    private final WalletRepository walletRepository;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.payos.webhook-url}")
    private String payosWebhookUrl;

    @Override
    @Transactional
    public PaymentLinkResponse createPaymentLink(Long projectId, String buyerEmail) {
        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Nguoi dung khong ton tai"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project khong ton tai"));

        validateProjectCanBePurchased(project, buyer);

        if (projectPurchaseRepository.existsByProjectIdAndUserId(project.getId(), buyer.getId())) {
            throw new RuntimeException("Ban da mua source code cua project nay");
        }
        BigDecimal price = project.getPriceDownload().setScale(2, RoundingMode.UNNECESSARY);
        Long orderCode = generateOrderCode();

        Order order = Order.builder()
                .orderCode(orderCode)
                .buyer(buyer)
                .project(project)
                .price(price)
                .status(OrderStatus.PENDING)
                .build();

        Payment payment = Payment.builder()
                .order(order)
                .price(price)
                .status(PaymentStatus.PENDING)
                .build();
        order.setPayment(payment);
        orderRepository.save(order);


        //CreatePaymentLinkRequest method của PayOs
        var paymentLinkRequest = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(toPayOSAmount(price))
                .description("Mua source code")
                .returnUrl(frontendUrl + "/payment-success?orderCode=" + orderCode)
                .cancelUrl(frontendUrl + "/payment-cancel?orderCode=" + orderCode)
                .build();

        try {
            var paymentLink = payOS.paymentRequests().create(paymentLinkRequest);
            //kết quả trả về link + thông tin cho FE điều hướng
            return PaymentLinkResponse.builder()
                    .orderId(order.getId())
                    .orderCode(order.getOrderCode())
                    .projectId(project.getId())
                    .price(order.getPrice())
                    .status(order.getStatus())
                    .checkoutUrl(paymentLink.getCheckoutUrl()) //link mở trang thanh toán
                    .build();
        } catch (Exception ex) {
            order.setStatus(OrderStatus.FAILED);
            payment.setStatus(PaymentStatus.FAILED);
            throw new RuntimeException("Tao link thanh toan payOS that bai: " + ex.getMessage());
        }
    }

    @Override
    @Transactional
    public void handlePayOSWebhook(Webhook webhook) {
        /// payOS gửi dữ liệu lỗi hoặc không có data thì bỏ qua
        if (webhook == null || webhook.getData() == null) {
            return;
        }

        /// phần chứa thông tin giao dịch như:
        /// orderCode
        /// amount
        /// code
        /// description
        /// transactionDateTime
        WebhookData requestData = webhook.getData();
        // nếu không có code & mã code là 123 do payos gửi về là 123 thì bỏ qua
        if (requestData.getOrderCode() == null || requestData.getOrderCode() == 123L) {
            return;
        }
        /// flow chính
        try {
            //xác minh webhook có thật từ payOS không.
            // webhooks().verify()
            WebhookData data = payOS.webhooks().verify(webhook);
            Order order = orderRepository.findByOrderCode(data.getOrderCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay order tu webhook payOS"));

            Payment payment = paymentRepository.findByOrderId(order.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay payment cua order"));

            String code = data.getCode();
            /// business chính : function markOrderPaid
            if ("00".equals(code)) {
                markOrderPaid(order, payment);
                return;
            }

            /// thanh toán fail -> set FAILED
            order.setStatus(OrderStatus.FAILED);
            payment.setStatus(PaymentStatus.FAILED);
        } catch (Exception ex) {
            throw new RuntimeException("Webhook payOS khong hop le: " + ex.getMessage());
        }
    }

//    @Override
//    public String confirmWebhook() {
//        try {
//            payOS.webhooks().confirm(payosWebhookUrl);
//            return payosWebhookUrl;
//        } catch (Exception ex) {
//            throw new RuntimeException("Khai bao webhook payOS that bai: " + ex.getMessage());
//        }
//    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderPaymentResponse> getMyOrders(String buyerEmail) {
        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Nguoi dung khong ton tai"));

        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyer.getId())
                .stream()
                .map(this::toOrderPaymentResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectPurchaseResponse> getMyPurchases(String buyerEmail) {
        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Nguoi dung khong ton tai"));

        return projectPurchaseRepository.findByUserIdOrderByCreatedAtDesc(buyer.getId())
                .stream()
                .map(purchase -> ProjectPurchaseResponse.builder()
                        .id(purchase.getId())
                        .projectId(purchase.getProject().getId())
                        .projectTitle(purchase.getProject().getTitle())
                        .sourceCodeUrl(purchase.getProject().getSourceCodeUrl())
                        .createdAt(purchase.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getMyWallet(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Nguoi dung khong ton tai"));

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseGet(() -> Wallet.builder()
                        .user(user)
                        .totalBalance(BigDecimal.ZERO)
                        .availableBalance(BigDecimal.ZERO)
                        .build());

        return WalletResponse.builder()
                .id(wallet.getId())
                .totalBalance(wallet.getTotalBalance())
                .availableBalance(wallet.getAvailableBalance())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    private void validateProjectCanBePurchased(Project project, User buyer) {
        if (project.getStatus() != ProjectStatus.APPROVED) {
            throw new RuntimeException("Chi duoc mua project da duoc duyet");
        }

        if (project.getPriceType() != PriceType.PAID) {
            throw new RuntimeException("Project nay khong phai project co phi");
        }

        if (project.getPriceDownload() == null || project.getPriceDownload().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Project chua co gia ban hop le");
        }

        User seller = project.getStudent().getUser();
        if (seller != null && seller.getId().equals(buyer.getId())) {
            throw new RuntimeException("Khong the mua source code cua chinh minh");
        }
    }

    private void markOrderPaid(Order order, Payment payment) {
        if (order.getStatus() == OrderStatus.PAID && payment.getStatus() == PaymentStatus.SUCCESS) {
            return;
        }

        order.setStatus(OrderStatus.PAID);
        payment.setStatus(PaymentStatus.SUCCESS);

        //lưu thông tin người mua tương ứng với project
        if (!projectPurchaseRepository.existsByProjectIdAndUserId(order.getProject().getId(), order.getBuyer().getId())) {
            projectPurchaseRepository.save(ProjectPurchase.builder()
                    .project(order.getProject())
                    .user(order.getBuyer())
                    .build());
        }

        Project project = order.getProject();
        /// khi thanh toán + 1 vào project
        project.setDownloadCount((project.getDownloadCount() == null ? 0 : project.getDownloadCount()) + 1);

        User seller = project.getStudent().getUser();
        Wallet wallet = walletRepository.findByUserId(seller.getId())
                .orElseGet(() -> Wallet.builder()
                        .user(seller)
                        .totalBalance(BigDecimal.ZERO)
                        .availableBalance(BigDecimal.ZERO)
                        .build());
        wallet.setTotalBalance(wallet.getTotalBalance().add(order.getPrice())); /// cộng tiền vào ví cho sinh viên
        wallet.setAvailableBalance(wallet.getAvailableBalance().add(order.getPrice()));
        walletRepository.save(wallet);
    }

    private Long generateOrderCode() {
        Long orderCode = System.currentTimeMillis();
        while (orderRepository.findByOrderCode(orderCode).isPresent()) {
            orderCode++;
        }
        return orderCode;
    }

    private Long toPayOSAmount(BigDecimal price) {
        return price.setScale(0, RoundingMode.UNNECESSARY).longValueExact();
    }

    private OrderPaymentResponse toOrderPaymentResponse(Order order) {
        Payment payment = order.getPayment();
        return OrderPaymentResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .projectId(order.getProject().getId())
                .projectTitle(order.getProject().getTitle())
                .price(order.getPrice())
                .orderStatus(order.getStatus())
                .paymentStatus(payment == null ? null : payment.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
