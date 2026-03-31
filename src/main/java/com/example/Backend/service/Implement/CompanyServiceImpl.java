package com.example.Backend.service.Implement;

import com.cloudinary.Cloudinary;
import com.example.Backend.cloudinary.CloudinaryService;
import com.example.Backend.dto.request.CompanyUpdateRequest;
import com.example.Backend.dto.request.admin.AdminUpdateCompanyRequest;
import com.example.Backend.dto.response.CompanyResponse;
import com.example.Backend.entity.Admin;
import com.example.Backend.entity.Company;
import com.example.Backend.entity.CompanyStatus;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.mapper.CompanyMapper;
import com.example.Backend.repository.AdminReponsitory;
import com.example.Backend.repository.CompanyReponsitory;
import com.example.Backend.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyReponsitory companyReponsitory;
    private final CompanyMapper companyMapper;
    private final AdminReponsitory adminReponsitory;
    private final CloudinaryService cloudinaryService;

    @Override
    public CompanyResponse getCompanyById(Long id) {
        Company company = companyReponsitory.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy công ty")
        );
        return companyMapper.toResponse(company);
    }

    @Override
    public List<CompanyResponse> getAllCompanies() {
        return companyMapper.toResponseList(companyReponsitory.findAll());
    }

    @Override
    public CompanyResponse updateStatusByAdmin(Long id, AdminUpdateCompanyRequest request) {
        Company company = companyReponsitory.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tim thấy công ty"));
        Admin admin = adminReponsitory.findById(request.getAdminId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Admin với ID: " + request.getAdminId()));
        company.setVerifiedBy(admin);
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                CompanyStatus newStatus = CompanyStatus.valueOf(request.getStatus().toUpperCase().trim());
                company.setStatus(newStatus);
                if (newStatus == CompanyStatus.APPROVED) {
                    company.setIsVerified(true);
                    company.setVerifiedAt(LocalDateTime.now());
                } else if (newStatus == CompanyStatus.REJECTED) {
                    company.setIsVerified(false);
                }
            } catch (IllegalArgumentException e) {

                throw new RuntimeException("Trạng thái '" + request.getStatus() + "' không hợp lệ. Chỉ chấp nhận: PENDING, APPROVED, REJECTED");
            }
        }
        Company updatedCompany = companyReponsitory.save(company);
        return companyMapper.toResponse(updatedCompany);
    }

    @Override
    public CompanyResponse update(CompanyUpdateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Company company = companyReponsitory.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy"));
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
            try {
                String oldAvatar = company.getAvatarUrl();
                String newAvatar = cloudinaryService.uploadAvatar(
                        request.getAvatarUrl(),
                        "avatars/companies",
                        "2"
                );
                if (oldAvatar != null && oldAvatar.contains("cloudinary.com")) {
                    String oldPublicId = cloudinaryService.extractPublicId(oldAvatar);
                    if (oldPublicId != null) {
                        cloudinaryService.deleteAvatar(oldPublicId, "2");
                    }
                }
                company.setAvatarUrl(newAvatar);
            } catch (Exception e) {
                //in lỗi ra console
                e.printStackTrace();
                throw new RuntimeException("Lỗi khi upload avt:" + e.getMessage());
            }
        }
        companyMapper.updateCompanyFromRequest(request, company);
        companyReponsitory.save(company);
        return companyMapper.toResponse(company);
    }

    @Override
    public void delete(Long id) {
        Company company = companyReponsitory.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy "));
        companyReponsitory.delete(company);
    }
}
