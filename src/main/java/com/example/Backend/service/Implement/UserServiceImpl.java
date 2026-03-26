package com.example.Backend.service.Implement;

import com.example.Backend.dto.request.LoginRequest;
import com.example.Backend.dto.response.LoginResponse;
import com.example.Backend.entity.*;
import com.example.Backend.mapper.StudentMapper;
import com.example.Backend.mapper.TeacherMapper;
import com.example.Backend.repository.TeacherRepository;
import com.example.Backend.security.JwtService;
import com.example.Backend.dto.request.RegisterRequest;
import com.example.Backend.dto.response.RegisterResponse;
import com.example.Backend.mapper.UserMapper;
import com.example.Backend.repository.StudentRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.EmailService;
import com.example.Backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.ThreadLocalRandom;





@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;  // gửi verify email
    private final JwtService jwtService;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) throws BadRequestException {

        //check email đã có trong hệ thống hay chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng. Vui lòng đăng nhập hoặc dùng email khác.");
        }
        // ====================== PHÂN QUYỀN TẠO TÀI KHOẢN ======================
        if (!request.isCreatedByAdmin()) {
            // ================== NGƯỜI DÙNG TỰ ĐĂNG KÝ ==================
            if (request.getRole() != RoleType.STUDENT && request.getRole() != RoleType.COMPANY) {
                throw new BadRequestException("Role không hợp lệ cho đăng ký tự động. Chỉ STUDENT và COMPANY được phép tự đăng ký.");
            }
        }
        else {
            // ================== ADMIN TẠO TÀI KHOẢN ==================
            // Chỉ cho phép tạo TEACHER và ADMIN
            if (request.getRole() != RoleType.TEACHER && request.getRole() != RoleType.ADMIN
                    && request.getRole() != RoleType.STUDENT && request.getRole() != RoleType.COMPANY) {

                throw new BadRequestException("Role không hợp lệ. Admin chỉ được tạo: STUDENT, COMPANY, TEACHER, ADMIN.");
            }
        }

        //pass hết: thực hiện tạo user trong db
        User user = userMapper.toEntity(request); ///dto -> entity
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setProvider(Provider.LOCAL);
        user.setActive(true);
        user.setEmailVerified(false);
        User savedUser = userRepository.save(user);


        ///tạo student
        if (request.getRole() == RoleType.STUDENT) {
            Student student = studentMapper.toEntity(request, savedUser);// dùng mapper
            studentRepository.save(student); // tạo student
        }
        /// tạo teacher
        else if (request.getRole() == RoleType.TEACHER) {
            Teacher teacher = teacherMapper.toEntity(request, savedUser);
            teacher.setEmployeeCode(
                    "GV" + ThreadLocalRandom.current()
                            .nextLong(1_000_000_000L, 10_000_000_000L)
            );
            teacherRepository.save(teacher);
        }
        /// tạo company
        /// tạo admin
//         Gửi email xác thực
        emailService.sendVerificationEmail(user); //gửi mail + kèm link xác thực chủ email

        return userMapper.toDto(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. Tìm user theo email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không đúng"));

        // 2. Kiểm tra mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email hoặc mật khẩu không đúng");
        }

        // 3. Kiểm tra email đã được xác thực chưa
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email chưa được xác thực. Vui lòng kiểm tra hộp thư.");
        }

        // 4. Kiểm tra tài khoản có bị khóa không
        if (!user.isActive()) {
            throw new RuntimeException("Tài khoản bị khóa.");
        }

        if (user.getRole() != request.getRole()) {
            throw new RuntimeException("Role không đúng. Tài khoản của bạn là "
                    + user.getRole().name()
                    + ". Vui lòng chọn đúng vai trò.");
        }

        // 5. Tạo access token (JWT)
        String accessToken = jwtService.generateToken(user);

        // 6. Build response theo DTO AuthResponse
        return LoginResponse.builder()
                .accessToken(accessToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .build();
    }

    @Override
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) throws BadRequestException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài khoản"));
        // Kiểm tra tài khoản có active không
        if (!user.isActive()) {
            throw new BadRequestException("Tài khoản đã bị khóa. Không thể đổi mật khẩu.");
        }
        // Kiểm tra mật khẩu cũ có đúng không
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Mật khẩu cũ không đúng.");
        }
        // Kiểm tra mật khẩu mới không được trùng mật khẩu cũ
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BadRequestException("Mật khẩu mới không được trùng với mật khẩu cũ.");
        }
        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public LoginResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return LoginResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}
