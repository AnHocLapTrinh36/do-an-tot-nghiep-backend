package com.example.Backend.service;

import com.example.Backend.dto.request.ApplicationReviewRequest;
import com.example.Backend.dto.request.JobApplyRequest;
import com.example.Backend.dto.response.ApplicantResponse;
import com.example.Backend.dto.response.JobApplicationResponse;
import com.example.Backend.entity.JobApplication;

import java.util.List;

public interface JobApplicationService {
    JobApplicationResponse applyJob(Long jobId, JobApplyRequest request);

    List<ApplicantResponse> getApplicantsByJob(Long jobId);

    List<ApplicantResponse> getJobApplicationsApproved();

    JobApplicationResponse reviewApplication(Long applicationId, ApplicationReviewRequest request);

    List<JobApplication> test(Long jobId);

    List<ApplicantResponse> getApplicationByCompany(String email);
}
