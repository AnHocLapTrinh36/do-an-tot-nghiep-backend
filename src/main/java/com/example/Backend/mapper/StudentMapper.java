package com.example.Backend.mapper;

import com.example.Backend.dto.request.RegisterRequest;
import com.example.Backend.dto.request.StudentUpdateRequest;
import com.example.Backend.dto.response.StudentResponse;
import com.example.Backend.entity.Student;
import com.example.Backend.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface StudentMapper {

    //Register (tạo mới Student)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "mssv", ignore = true)
    @Mapping(target = "gpa", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "status", constant = "Đang học")
    Student toEntity(RegisterRequest request);

//  Dùng khi Update thông tin Student
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "mssv", ignore = true)// Không cho phép thay đổi User
    void updateEntity(@MappingTarget Student student, StudentUpdateRequest request);

//     Map Entity → Response (trả về cho client)
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "fullName", source = "user.fullName")
    StudentResponse toResponse(Student student);

    // Map danh sách
    List<StudentResponse> toResponseList(List<Student> students);
}