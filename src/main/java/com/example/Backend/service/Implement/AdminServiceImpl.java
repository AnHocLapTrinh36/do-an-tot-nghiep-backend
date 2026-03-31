package com.example.Backend.service.Implement;

import com.example.Backend.dto.request.admin.AdminUpdateRequest;
import com.example.Backend.dto.response.AdminResponse;
import com.example.Backend.entity.Admin;
import com.example.Backend.entity.User;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.mapper.AdminMapper;
import com.example.Backend.repository.AdminRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final AdminMapper adminMapper;

    @Override
    public AdminResponse getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Admin admin = adminRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin Admin cho user hiện tại"));

        return adminMapper.toResponse(admin);
    }

    @Override
    @Transactional
    public AdminResponse updateMyProfile(AdminUpdateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Admin admin = adminRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin Admin cho user hiện tại"));

        // 1. Cập nhật bảng Users (fullName)
        User user = admin.getUser();
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
            userRepository.save(user); // Lưu cập nhật tên vào bảng Users
        }

        // 2. Cập nhật bảng Admins (position, phone)
        if (request.getPosition() != null) {
            admin.setPosition(request.getPosition().trim());
        }
        if (request.getPhone() != null) {
            admin.setPhone(request.getPhone().trim());
        }

        Admin updatedAdmin = adminRepository.save(admin);

        return adminMapper.toResponse(updatedAdmin);
    }
}
