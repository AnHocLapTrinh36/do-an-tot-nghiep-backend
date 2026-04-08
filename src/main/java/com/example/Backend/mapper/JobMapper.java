package com.example.Backend.mapper;

import com.example.Backend.dto.response.JobResponse;
import com.example.Backend.entity.JobPost;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface JobMapper {
    // Tự động map company.companyName -> companyName và company.avatarUrl -> companyLogo
    @Mapping(source = "company.companyName", target = "companyName")
    @Mapping(source = "company.avatarUrl", target = "companyLogo")
    @Mapping(source = "jdFile", target = "jdFileUrl")
    JobResponse toResponse(JobPost jobPost);

    List<JobResponse> toResponseList(List<JobPost> jobPosts);
}
