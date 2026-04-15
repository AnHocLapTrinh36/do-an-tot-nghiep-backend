package com.example.Backend.mapper;

import com.example.Backend.dto.response.JobResponse;
import com.example.Backend.entity.JobPost;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface JobMapper {
    // Tự động map company.companyName -> companyName và company.avatarUrl -> companyLogo
    @Mapping(source = "company.companyName", target = "companyName")
    @Mapping(source = "company.avatarUrl", target = "companyLogo")
    @Mapping(source = "jdFile", target = "jdFileUrl")
    @Mapping(source = "tags", target = "tags", qualifiedByName = "mapTags")
    JobResponse toResponse(JobPost jobPost);

    @Named("mapTags")
    default List<String> mapTags(String tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            // Dùng ObjectMapper của Jackson để parse
            //chuyển từ chuỗi string sang mảng
            return new ObjectMapper().readValue(tags, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            // Nếu lỗi parse thì trả về list rỗng để tránh crash app
            return Collections.emptyList();
        }
    }

    List<JobResponse> toResponseList(List<JobPost> jobPosts);
}
