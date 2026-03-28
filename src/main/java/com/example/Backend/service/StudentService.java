package com.example.Backend.service;


import com.example.Backend.dto.request.StudentUpdateRequest;
import com.example.Backend.dto.request.admin.AdminUpdateStudentRequest;
import com.example.Backend.dto.response.StudentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


@Service
public interface StudentService {
    List<StudentResponse> getAllStudents();
    StudentResponse getStudentById(Long id);
    StudentResponse updateMyProfile(StudentUpdateRequest request) throws IOException;
    StudentResponse adminUpdateStudent(Long id, AdminUpdateStudentRequest request);
    void deleteStudent(Long id);
    StudentResponse getMyProfile(String email);

}
