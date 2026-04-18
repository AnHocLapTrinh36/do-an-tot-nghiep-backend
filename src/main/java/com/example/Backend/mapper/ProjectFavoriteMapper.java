package com.example.Backend.mapper;

import com.example.Backend.dto.response.ProjectFavoriteResponse;
import com.example.Backend.entity.ProjectFavorite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectFavoriteMapper {

    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.title", target = "projectTitle")
    @Mapping(source = "project.student.user.fullName", target = "studentName")
    ProjectFavoriteResponse toResponse(ProjectFavorite projectFavorite);

    List<ProjectFavoriteResponse> toResponseList(List<ProjectFavorite> projectFavorites);
}
