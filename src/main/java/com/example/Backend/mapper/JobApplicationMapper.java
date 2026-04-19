package com.example.Backend.mapper;

import com.example.Backend.dto.response.ApplicantResponse;
import com.example.Backend.dto.response.JobApplicationResponse;
import com.example.Backend.entity.JobApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface JobApplicationMapper {
    //cho student
    @Mapping(source = "jobPost.title", target = "jobTitle")
    @Mapping(source = "student.user.fullName", target = "studentName")
    @Mapping(source = "cv.title", target = "cvTitle")
    @Mapping(source = "status", target = "status")
    JobApplicationResponse toResponse(JobApplication entity);

    //cho company
    @Mapping(source = "id", target = "applicationId")
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.user.fullName", target = "fullName")
    @Mapping(source = "student.user.email", target = "email")
    @Mapping(source = "cv.title", target = "cvTitle")
    @Mapping(source = "jobPost.title", target = "jobTitle")
    ApplicantResponse toApplicantResponse(JobApplication entity);

    List<ApplicantResponse> toApplicantResponseList(List<JobApplication> entities);

}