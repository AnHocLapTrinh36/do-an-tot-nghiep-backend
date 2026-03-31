package com.example.Backend.service;

import com.example.Backend.dto.request.admin.AdminUpdateRequest;
import com.example.Backend.dto.response.AdminResponse;

public interface AdminService {

    AdminResponse getMyProfile();
    AdminResponse updateMyProfile(AdminUpdateRequest request);
}
