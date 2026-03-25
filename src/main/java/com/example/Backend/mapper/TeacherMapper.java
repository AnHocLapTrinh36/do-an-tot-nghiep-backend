package com.example.Backend.mapper;

import com.example.Backend.dto.Teacher.TeacherRequest;
import com.example.Backend.dto.Teacher.TeacherResponse;
import com.example.Backend.dto.Teacher.TeacherUpdateRequest;
import com.example.Backend.entity.Teacher;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TeacherMapper {
    // bên teacherRequest chứa các trường user, nhưng không có trường nào tên là user nên dòng này giúp không được động trường user
    @Mapping(target = "user", ignore = true)
    Teacher toEntity(TeacherRequest request);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.fullName", target = "fullName")
    TeacherResponse toResponse(Teacher teacher);

    List<TeacherResponse> toResponseList(List<Teacher> teachers);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    void updateTeacherFromUpdateDto(TeacherUpdateRequest request, @MappingTarget Teacher teacher);

}
