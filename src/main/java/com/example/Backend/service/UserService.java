package com.example.Backend.service;

import com.example.Backend.dto.request.LoginRequest;
import com.example.Backend.dto.request.RegisterRequest;
import com.example.Backend.dto.response.LoginResponse;
import com.example.Backend.dto.response.RegisterResponse;
import com.example.Backend.entity.User;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;


@Service
public interface UserService {
//    List<UserReponse> getAllUsers();
//    User updateUser(Long id, AdminUserUpdateRequest updateUser);
//    void deleteUser(Long id);
    RegisterResponse register(RegisterRequest request) throws BadRequestException;
    LoginResponse login(LoginRequest request) throws BadRequestException;
    User findByEmail(String email);
    boolean existsByEmail(String email);
    void changePassword(String email, String oldPassword, String newPassword) throws BadRequestException;
    LoginResponse getUserByEmail(String email);
    LoginResponse getCurrentUser();
}
