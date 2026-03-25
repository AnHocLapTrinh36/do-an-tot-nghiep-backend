package com.example.Backend.service;

import com.example.Backend.dto.response.TeacherResponse;
import com.example.Backend.entity.User;

import java.util.List;

public interface TeacherService {
//    TeacherResponse createTeacher(TeacherRequest request);

    TeacherResponse getTeacherById(Long id,  User currentUser);

    List<TeacherResponse> getAllTeachers();

//    TeacherResponse updateTeacher(Long id, TeacherUpdateRequest request);

    void deleteTeacher(Long id);
}
