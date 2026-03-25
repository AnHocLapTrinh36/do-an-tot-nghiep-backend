package com.example.Backend.service.User.Impl;

import com.example.Backend.dto.User.AdminUserRequest;
import com.example.Backend.dto.User.AdminUserUpdateRequest;
import com.example.Backend.dto.User.UserReponse;
import com.example.Backend.entity.User;
import com.example.Backend.mapper.UserMapper;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.User.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserReponse createUser(AdminUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Lỗi: Email này đã được sử dụng");
        }
        User newUser = userMapper.toEntity(request);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setIsActive(true);
        User saveUser = userRepository.save(newUser);
        return userMapper.toResponse(saveUser);
    }

    @Override
    public List<UserReponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toResponseList(users);
    }

    @Override
    public User updateUser(Long id, AdminUserUpdateRequest updateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getEmail().equals(updateDto.getEmail())) {
            if (userRepository.existsByEmail(updateDto.getEmail())) {
                throw new RuntimeException("Email đã được dùng");
            }
        }

        userMapper.updateEntityFromDto(updateDto, user);

        if (updateDto.getPassword() != null && !updateDto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id))
            throw new RuntimeException("User không tồn tại để xóa");
        userRepository.deleteById(id);
    }
}
