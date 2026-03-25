package com.example.Backend.mapper;

import com.example.Backend.dto.request.RegisterRequest;
import com.example.Backend.entity.Student;
import com.example.Backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface StudentMapper {

    // 1. Dùng khi Register (tạo mới Student)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "mssv", ignore = true)
    @Mapping(target = "gpa", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "status", constant = "Đang học")
    Student toEntity(RegisterRequest request, User user);

    // 2. Dùng khi Update thông tin Student (RẤT QUAN TRỌNG)
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "user", ignore = true)           // Không cho phép thay đổi User
//    void updateEntity(@MappingTarget Student student, StudentUpdateRequest request);

    // 3. Map Entity → Response (trả về cho client)
//    @Mapping(target = "userId", source = "user.id")
//    @Mapping(target = "email", source = "user.email")
//    @Mapping(target = "fullName", source = "user.fullName")
//    StudentResponse toResponse(Student student);

    // Map danh sách
//    List<StudentResponse> toResponseList(List<Student> students);
}