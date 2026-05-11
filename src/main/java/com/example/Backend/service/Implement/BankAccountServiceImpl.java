package com.example.Backend.service.Implement;

import com.example.Backend.dto.request.BankAccountRequest;
import com.example.Backend.dto.response.BankAccountResponse;
import com.example.Backend.entity.BankAccount;
import com.example.Backend.entity.User;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.repository.BankAccountRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BankAccountResponse create(BankAccountRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Nguoi dung khong ton tai"));

        BankAccount bankAccount = BankAccount.builder()
                .user(user)
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .build();

        return toResponse(bankAccountRepository.save(bankAccount));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountResponse> getMyBankAccounts(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Nguoi dung khong ton tai"));

        return bankAccountRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Nguoi dung khong ton tai"));

        BankAccount bankAccount = bankAccountRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tai khoan ngan hang khong ton tai"));

        bankAccountRepository.delete(bankAccount);
    }

    private BankAccountResponse toResponse(BankAccount bankAccount) {
        return BankAccountResponse.builder()
                .id(bankAccount.getId())
                .bankName(bankAccount.getBankName())
                .accountNumber(bankAccount.getAccountNumber())
                .createdAt(bankAccount.getCreatedAt())
                .build();
    }
}
