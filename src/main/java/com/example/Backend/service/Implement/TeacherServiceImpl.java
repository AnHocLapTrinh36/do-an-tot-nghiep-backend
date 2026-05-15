package com.example.Backend.service.Implement;

import com.example.Backend.dto.request.TeacherUpdateRequest;
import com.example.Backend.dto.response.TeacherResponse;
import com.example.Backend.entity.RoleType;
import com.example.Backend.entity.Teacher;
import com.example.Backend.entity.User;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.mapper.TeacherMapper;
import com.example.Backend.repository.TeacherRepository;
import com.example.Backend.service.TeacherService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {
    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;
    /// chi tiết 1 GV
    @Override
    public TeacherResponse getTeacherById(Long id, User currentUser) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));

        // Nếu là TEACHER thì chỉ được xem thông tin của chính mình
        if (currentUser.getRole() == RoleType.TEACHER) {
            if (!teacher.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Bạn chỉ được xem thông tin của chính mình");
            }
        }
        return teacherMapper.toResponse(teacher);
    }
    /// danh sach GV
    @Override
    public List<TeacherResponse> getAllTeachers() {
        return teacherMapper.toResponseList(teacherRepository.findAll());
    }

    @Override
    @Transactional
    public TeacherResponse updateMyProfile(String email, TeacherUpdateRequest request) {
        Teacher teacher = teacherRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));

        teacher.getUser().setFullName(request.getFullName().trim());
        return teacherMapper.toResponse(teacherRepository.save(teacher));
    }

//    @Override
//    @Transactional
//    public TeacherResponse updateTeacher(Long id, TeacherUpdateRequest request) {
//
//        Teacher teacher = teacherRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên để cập nhật"));
//
//        teacherMapper.updateTeacherFromUpdateDto(request, teacher);
//        User user = teacher.getUser();
//        if (request.getFullName() != null) user.setFullName(request.getFullName());
//        if (request.getEmail() != null) {
//            if (!user.getEmail().equals(request.getEmail()) &&
//                    userRepository.existsByEmail(request.getEmail())) {
//                throw new RuntimeException("Email mới đã tồn tại hệ thống!");
//            }
//            user.setEmail(request.getEmail());
//        }
//
//        if (request.getPassword() != null && !request.getPassword().isBlank()) {
//            user.setPassword(passwordEncoder.encode(request.getPassword()));
//        }
//        userRepository.save(user);
//        return teacherMapper.toResponse(teacherRepository.save(teacher));
//    }
    /// Xóa 1 GV
    @Override
    @Transactional
    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên để xóa"));
        teacherRepository.delete(teacher);
    }

}
