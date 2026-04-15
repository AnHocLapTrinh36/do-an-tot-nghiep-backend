package com.example.Backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {
    private Long id;
    private String title;
    private String companyName;
    private String companyLogo;
    private String status;
    private Double salary;
    private String jdFileUrl;
    private Integer viewCount;
    private String jobType;
    private String workLocation;
    private LocalDateTime startDay;
    private LocalDateTime endDay;
    private List<String> tags;

}
