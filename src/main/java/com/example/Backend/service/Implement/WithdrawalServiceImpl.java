package com.example.Backend.service.Implement;

import com.example.Backend.dto.request.AdminUpdateWithdrawalRequest;
import com.example.Backend.dto.request.CreateWithdrawalRequest;
import com.example.Backend.dto.response.WithdrawalResponse;
import com.example.Backend.entity.*;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.repository.BankAccountRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.repository.WalletRepository;
import com.example.Backend.repository.WithdrawalRepository;
import com.example.Backend.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {

    private final WithdrawalRepository withdrawalRepository;
    private final WalletRepository walletRepository;
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public WithdrawalResponse create(CreateWithdrawalRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Nguoi dung khong ton tai"));

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Vi chua co so du"));

        if (wallet.getAvailableBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("So du kha dung khong du de rut tien");
        }

        BankAccount bankAccount = bankAccountRepository.findByIdAndUserId(request.getBankAccountId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tai khoan ngan hang khong ton tai"));

        wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(request.getAmount()));

        Withdrawal withdrawal = Withdrawal.builder()
                .user(user)
                .wallet(wallet)
                .amount(request.getAmount())
                .bankAccount(bankAccount)
                .status(WithdrawalStatus.PENDING)
                .build();

        return toResponse(withdrawalRepository.save(withdrawal));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WithdrawalResponse> getMyWithdrawals(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Nguoi dung khong ton tai"));

        return withdrawalRepository.findByUserIdOrderByRequestedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WithdrawalResponse> getAllWithdrawals() {
        return withdrawalRepository.findAllByOrderByRequestedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public WithdrawalResponse updateByAdmin(Long id, AdminUpdateWithdrawalRequest request, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin khong ton tai"));

        Withdrawal withdrawal = withdrawalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yeu cau rut tien khong ton tai"));

        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new RuntimeException("Yeu cau rut tien da duoc xu ly");
        }

        if (request.getStatus() == WithdrawalStatus.PENDING) {
            throw new RuntimeException("Admin chi duoc chuyen sang COMPLETED hoac REJECTED");
        }

        withdrawal.setStatus(request.getStatus());
        withdrawal.setAdminNote(request.getAdminNote());
        withdrawal.setProcessedBy(admin);

        if (request.getStatus() == WithdrawalStatus.REJECTED) {
            Wallet wallet = withdrawal.getWallet();
            BigDecimal currentAvailable = wallet.getAvailableBalance() == null ? BigDecimal.ZERO : wallet.getAvailableBalance();
            wallet.setAvailableBalance(currentAvailable.add(withdrawal.getAmount()));
        }

        if (request.getStatus() == WithdrawalStatus.APPROVED) {
            Wallet wallet = withdrawal.getWallet();
            wallet.setTotalBalance(wallet.getTotalBalance().subtract(withdrawal.getAmount()));
        }

        return toResponse(withdrawal);
    }

    private WithdrawalResponse toResponse(Withdrawal withdrawal) {
        return WithdrawalResponse.builder()
                .id(withdrawal.getId())
                .userId(withdrawal.getUser().getId())
                .userEmail(withdrawal.getUser().getEmail())
                .amount(withdrawal.getAmount())
                .status(withdrawal.getStatus())
                .bankName(withdrawal.getBankAccount().getBankName())
                .accountNumber(withdrawal.getBankAccount().getAccountNumber())
                .adminNote(withdrawal.getAdminNote())
                .requestedAt(withdrawal.getRequestedAt())
                .updatedAt(withdrawal.getUpdatedAt())
                .build();
    }
}
