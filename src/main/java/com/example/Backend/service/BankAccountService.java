package com.example.Backend.service;

import com.example.Backend.dto.request.BankAccountRequest;
import com.example.Backend.dto.response.BankAccountResponse;

import java.util.List;

public interface BankAccountService {
    BankAccountResponse create(BankAccountRequest request, String email);

    List<BankAccountResponse> getMyBankAccounts(String email);

    void delete(Long id, String email);
}
