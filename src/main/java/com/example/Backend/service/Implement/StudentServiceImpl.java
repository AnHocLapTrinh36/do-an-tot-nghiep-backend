package com.example.Backend.service.Implement;

import com.example.Backend.cloudinary.CloudinaryService;
import com.example.Backend.dto.request.StudentUpdateRequest;
import com.example.Backend.dto.request.admin.AdminUpdateStudentRequest;
import com.example.Backend.dto.response.StudentResponse;
import com.example.Backend.entity.Student;
import com.example.Backend.entity.User;
import com.example.Backend.entity.VerificationToken;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.mapper.StudentMapper;
import com.example.Backend.repository.StudentRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.repository.VerificationTokenRepository;
import com.example.Backend.service.StudentService;
import com.example.Backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    @Override
    public List<StudentResponse> getAllStudents() {
        log.info("Fetching all students list");

        List<Student> students = studentRepository.findAll();

        List<StudentResponse> responses = studentMapper.toResponseList(students);

        return responses;
    }

    @Override
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id).orElse(null);

        if (student == null) {
            throw new RuntimeException("Không tìm thấy sinh viên với ID: " + id);
        }

        StudentResponse response = studentMapper.toResponse(student);

        return response;
    }

    @Override
    @Transactional
    public StudentResponse updateMyProfile(StudentUpdateRequest request) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy profile của bạn"));

        log.info("Student {} is updating their own profile", email);

        // XỬ LÝ AVATAR
        /// check null và k rỗng
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            try {
                /// lấy hình ảnh cũ của student
                String oldAvatarUrl = student.getAvatarUrl();

                log.info("Uploading new avatar for student: {}", email);

                // Upload lên Cloudinary
                String newAvatarUrl = cloudinaryService.uploadAvatar(
                        request.getAvatar(),
                        "avatars/student",
                        "1"        // thay bằng "2" nếu muốn dùng account 2
                );
                if (newAvatarUrl != null) {
                    // Xóa avatar cũ (nếu là của Cloudinary)
                    if (oldAvatarUrl != null && oldAvatarUrl.contains("cloudinary.com")) {
                        String oldPublicId = cloudinaryService.extractPublicId(oldAvatarUrl);
                        if (oldPublicId != null) {
                            cloudinaryService.deleteAvatar(oldPublicId, "1");   // hoặc "2" tùy account  đang dùng
                        }
                    }
                    /// set avatar mới vào student
                    student.setAvatarUrl(newAvatarUrl);
                    log.info("Avatar updated successfully. New URL: {}", newAvatarUrl);
                }

            } catch (Exception e) {
                log.error("Failed to upload avatar for student {}", email, e);
                 throw new RuntimeException("Không thể upload avatar");
            }
        }
        studentMapper.updateEntity(student, request);
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            student.getUser().setFullName(request.getFullName().trim());
        }

        Student updatedStudent = studentRepository.save(student);

        return studentMapper.toResponse(updatedStudent);
    }

    @Override
    @Transactional
    public StudentResponse adminUpdateStudent(Long id, AdminUpdateStudentRequest request) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên với ID: " + id));

        log.info("Admin cập nhật GPA (thang 10) và status cho sinh viên ID: {}", id);

        // Cập nhật Status
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            student.setStatus(request.getStatus().trim());
        }

        // Cập nhật GPA
        if (request.getGpa() != null) {
            student.setGpa(request.getGpa());
        }
        Student updatedStudent = studentRepository.save(student);
        return studentMapper.toResponse(updatedStudent);
    }

    @Override
    @Transactional
    public void deleteStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Sinh viên không tồn tại"));

        User user = student.getUser();
        if (user == null) {
            throw new RuntimeException("Không tìm thấy tài khoản user liên kết với sinh viên này");
        }
        Long userId = user.getId();
        verificationTokenRepository.deleteByUserId(userId);
        studentRepository.delete(student);
        userRepository.delete(user);
        log.info("Đã xóa thành công sinh viên ID: {}", studentId);
    }

    @Override
    public StudentResponse getMyProfile(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên với email: " + email));
        return studentMapper.toResponse(student);
    }
}
