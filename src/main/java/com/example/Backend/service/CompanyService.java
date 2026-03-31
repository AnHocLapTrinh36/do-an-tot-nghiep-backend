package com.example.Backend.service;

import com.example.Backend.dto.request.CompanyUpdateRequest;
import com.example.Backend.dto.request.admin.AdminUpdateCompanyRequest;
import com.example.Backend.dto.response.CompanyResponse;
import com.example.Backend.repository.CompanyReponsitory;

import java.util.List;

public interface CompanyService {
    List<CompanyResponse> getAllCompanies();

    CompanyResponse getCompanyById(Long id);

    CompanyResponse updateStatusByAdmin(Long id, AdminUpdateCompanyRequest request);

    CompanyResponse update(CompanyUpdateRequest request);

    void delete(Long id);
}
