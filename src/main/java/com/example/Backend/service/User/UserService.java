package com.example.Backend.service.User;

import com.example.Backend.dto.User.AdminUserRequest;
import com.example.Backend.dto.User.AdminUserUpdateRequest;
import com.example.Backend.dto.User.UserReponse;
import com.example.Backend.entity.User;


import java.util.List;


public interface UserService {
    UserReponse createUser(AdminUserRequest user);

    List<UserReponse> getAllUsers();

    User updateUser(Long id, AdminUserUpdateRequest updateUser);

    void deleteUser(Long id);

}
