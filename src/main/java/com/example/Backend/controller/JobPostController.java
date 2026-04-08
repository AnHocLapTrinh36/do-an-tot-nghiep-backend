package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.JobRequest;
import com.example.Backend.dto.response.JobResponse;
import com.example.Backend.entity.JobPost;
import com.example.Backend.repository.JobPostRepository;
import com.example.Backend.service.JobPostService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobPostController {
    private final JobPostService jobPostService;


    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết tin tuyển dụng - public")
    public ApiResponse<JobResponse> getJobDetail(@PathVariable Long id) {
        return ApiResponse.success("Lấy chi tiết thành công", jobPostService.getJobDetail(id));
    }

    @GetMapping("/active")
    @Operation(summary = "Lấy danh sách tin đang tuyển đã duyệt, còn hạn - public")
    public ApiResponse<List<JobResponse>> getActiveJobs() {
        return ApiResponse.success("Lấy danh sách thành công", jobPostService.getActiveJobs());
    }
}
