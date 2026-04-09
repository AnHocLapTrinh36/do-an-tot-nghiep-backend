package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.JobApplyRequest;
import com.example.Backend.dto.response.JobApplicationResponse;
import com.example.Backend.service.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @PostMapping("/{jobId}/apply")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Sinh viên nộp đơn ứng tuyển cho một công việc - role(student)")
    public ResponseEntity<ApiResponse<JobApplicationResponse>> applyJob(
            @PathVariable Long jobId,
            @Valid @RequestBody JobApplyRequest request) {

        JobApplicationResponse response = jobApplicationService.applyJob(jobId, request);

        return ResponseEntity.ok(ApiResponse.success("Ứng tuyển thành công!", response));
    }

}