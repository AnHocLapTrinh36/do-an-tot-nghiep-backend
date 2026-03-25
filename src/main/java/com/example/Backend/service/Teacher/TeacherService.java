package com.example.Backend.service.Teacher;

import com.example.Backend.dto.ResponseApi.ApiResponse;
import com.example.Backend.dto.Teacher.TeacherRequest;
import com.example.Backend.dto.Teacher.TeacherResponse;
import com.example.Backend.dto.Teacher.TeacherUpdateRequest;

import java.util.List;

public interface TeacherService {
    TeacherResponse createTeacher(TeacherRequest request);

    TeacherResponse getTeacherById(Long id);

    List<TeacherResponse> getAllTeachers();

    TeacherResponse updateTeacher(Long id, TeacherUpdateRequest request);

    void deleteTeacher(Long id);
}
