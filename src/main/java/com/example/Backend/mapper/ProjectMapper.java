package com.example.Backend.mapper;

import com.example.Backend.dto.request.ProjectUpdateRequest;
import com.example.Backend.dto.response.ProjectImageResponse;
import com.example.Backend.dto.response.ProjectMemberResponse;
import com.example.Backend.dto.response.ProjectResponse;
import com.example.Backend.dto.response.TechnologyResponse;
import com.example.Backend.entity.Project;
import com.example.Backend.entity.ProjectImage;
import com.example.Backend.entity.ProjectMember;
import com.example.Backend.entity.Technology;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    // Ánh xạ các trường có tên khác nhau hoặc nằm sâu bên trong các Entity khác
    @Mapping(source = "student.user.fullName", target = "studentName")
    @Mapping(source = "student.mssv", target = "studentMssv")
    @Mapping(source = "course.name", target = "courseName")
    ProjectResponse toResponse(Project project);

    // MapStruct sẽ tự động gọi hàm này khi lặp qua mảng project.getTechnologies()
    TechnologyResponse toTechnologyResponse(Technology technology);

    // MapStruct sẽ tự động gọi hàm này khi lặp qua mảng project.getImages()
    ProjectImageResponse toProjectImageResponse(ProjectImage projectImage);

    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.user.fullName", target = "studentName")
    @Mapping(source = "student.mssv", target = "mssv")
    ProjectMemberResponse toProjectMemberResponse(ProjectMember projectMember);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProject(ProjectUpdateRequest request, @MappingTarget Project project);
}
