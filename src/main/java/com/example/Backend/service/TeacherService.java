package com.example.Backend.service;

import com.example.Backend.dto.request.TeacherUpdateRequest;
import com.example.Backend.dto.response.TeacherResponse;
import com.example.Backend.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TeacherService {
//    TeacherResponse createTeacher(TeacherRequest request);

    TeacherResponse getTeacherById(Long id,  User currentUser);

    List<TeacherResponse> getAllTeachers();

//    TeacherResponse updateTeacher(Long id, TeacherUpdateRequest request);

    TeacherResponse updateMyProfile(String email, TeacherUpdateRequest request);

    void deleteTeacher(Long id);
}
