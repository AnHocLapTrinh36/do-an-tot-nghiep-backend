package com.example.Backend.service;

import com.example.Backend.dto.request.AdminUpdateWithdrawalRequest;
import com.example.Backend.dto.request.CreateWithdrawalRequest;
import com.example.Backend.dto.response.WithdrawalResponse;

import java.util.List;

public interface WithdrawalService {
    WithdrawalResponse create(CreateWithdrawalRequest request, String email);

    List<WithdrawalResponse> getMyWithdrawals(String email);

    List<WithdrawalResponse> getAllWithdrawals();

    WithdrawalResponse updateByAdmin(Long id, AdminUpdateWithdrawalRequest request, String adminEmail);
}
