package com.example.Backend.service.Teacher.Impl;

import com.example.Backend.dto.ResponseApi.ApiResponse;
import com.example.Backend.dto.Teacher.TeacherRequest;
import com.example.Backend.dto.Teacher.TeacherResponse;
import com.example.Backend.dto.Teacher.TeacherUpdateRequest;
import com.example.Backend.entity.RoleType;
import com.example.Backend.entity.Teacher;
import com.example.Backend.entity.User;
import com.example.Backend.mapper.TeacherMapper;
import com.example.Backend.repository.TeacherRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.Teacher.TeacherService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TeacherMapper teacherMapper;

    @Override
    @Transactional // Đảm bảo lưu cả 2 bảng hoặc không bảng nào cả
    public TeacherResponse createTeacher(TeacherRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Lỗi: Email đã được sử dụng!");
        }
        if (teacherRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new RuntimeException("Lỗi: Mã giảng viên đã tồn tại!");
        }
        String password = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(password)
                .role(RoleType.TEACHER)
                .isActive(true)
                .build();
        user = userRepository.save(user);

        Teacher teacher = Teacher.builder()
                .employeeCode(request.getEmployeeCode())
                .user(user)
                .build();
        teacher = teacherRepository.save(teacher);

        return teacherMapper.toResponse(teacher);
    }

    @Override
    public TeacherResponse getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên với ID: " + id));
        return teacherMapper.toResponse(teacher);
    }

    @Override
    public List<TeacherResponse> getAllTeachers() {
        return teacherMapper.toResponseList(teacherRepository.findAll());
    }

    @Override
    @Transactional
    public TeacherResponse updateTeacher(Long id, TeacherUpdateRequest request) {

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên để cập nhật"));

        teacherMapper.updateTeacherFromUpdateDto(request, teacher);
        User user = teacher.getUser();
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getEmail() != null) {
            if (!user.getEmail().equals(request.getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email mới đã tồn tại hệ thống!");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);
        return teacherMapper.toResponse(teacherRepository.save(teacher));
    }

    @Override
    @Transactional
    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên để xóa"));
        teacherRepository.delete(teacher);
    }

}
