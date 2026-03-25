package com.example.Backend.controller.Teacher;

import com.example.Backend.dto.ResponseApi.ApiResponse;
import com.example.Backend.dto.Teacher.TeacherRequest;
import com.example.Backend.dto.Teacher.TeacherResponse;
import com.example.Backend.dto.Teacher.TeacherUpdateRequest;
import com.example.Backend.service.Teacher.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {
    private final TeacherService teacherService;

    @PostMapping
    public ApiResponse create(@Valid @RequestBody TeacherRequest request) {
        TeacherResponse result = teacherService.createTeacher(request);
        return ApiResponse.builder()
                .code(201)
                .message("Tạo thành công")
                .result(result)
                .build();
    }

    @GetMapping
    public ApiResponse getAll() {
        return ApiResponse.builder()
                .code(200)
                .result(teacherService.getAllTeachers())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse getById(@PathVariable Long id) {
        return ApiResponse.<TeacherResponse>builder()
                .code(200)
                .result(teacherService.getTeacherById(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse update(@PathVariable Long id, @Valid @RequestBody TeacherUpdateRequest request) {
        return ApiResponse.builder()
                .code(200)
                .message("Cập nhật thông tin thành công!")
                .result(teacherService.updateTeacher(id, request))
                .build();
    }

    // 5. Xóa Teacher
    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ApiResponse.builder()
                .code(200)
                .message("Xóa giáo viên thành công!")
                .build();
    }
}
