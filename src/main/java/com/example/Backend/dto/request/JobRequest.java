package com.example.Backend.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobRequest {
    private String title;
    private String description;
    private String requirements;
    private Double salary;
    private String jobType;
    private String workLocation;
    private LocalDateTime startDay;
    private LocalDateTime endDay;
    private MultipartFile jdFile;
    private List<String> tags;
}
