package com.example.Backend.mapper;

import com.example.Backend.dto.response.JobFavoriteResponse;
import com.example.Backend.entity.JobFavorite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface JobFavoriteMapper {
    @Mapping(source = "jobPost.id", target = "jobId")
    @Mapping(source = "jobPost.title", target = "jobTitle")
    @Mapping(source = "jobPost.company.companyName", target = "companyName")
    JobFavoriteResponse toResponse(JobFavorite favorite);

    List<JobFavoriteResponse> toResponseList(List<JobFavorite> favorites);
}
