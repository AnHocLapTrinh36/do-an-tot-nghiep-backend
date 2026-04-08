package com.example.Backend.service;

import com.example.Backend.dto.request.JobRequest;
import com.example.Backend.dto.response.JobResponse;

import java.util.List;


public interface JobPostService {
    JobResponse createJob(JobRequest request, String email);

    JobResponse updateJob(Long id, JobRequest request, String email);

    void closeJob(Long id);

    List<JobResponse> getActiveJobs();

    List<JobResponse> getPendingJobsForAdmin();

    JobResponse approveJob(Long id, String action, String adminEmail);

    JobResponse getJobDetail(Long id);
}
