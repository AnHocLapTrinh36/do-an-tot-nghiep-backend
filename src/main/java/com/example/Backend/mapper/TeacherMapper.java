package com.example.Backend.mapper;

import com.example.Backend.dto.request.RegisterRequest;
import com.example.Backend.dto.response.TeacherResponse;
import com.example.Backend.entity.Teacher;
import com.example.Backend.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TeacherMapper {
    // 1. Dùng khi Register
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    Teacher toEntity(RegisterRequest request);

    // 2. Dùng khi Update
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "user", ignore = true)        // không cho phép thay đổi user
//    void updateEntity(@MappingTarget Teacher teacher, TeacherUpdateRequest request);

    // 3. Map từ Entity → Response (dùng để trả về API)
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "fullName", source = "user.fullName")
    TeacherResponse toResponse(Teacher teacher);

    // Nếu cần map List
    List<TeacherResponse> toResponseList(List<Teacher> teachers);

}
