package com.example.Backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class StudentResponse {
    private Long id;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("user_id")
    private Long userId;
    private String email;
    private String mssv;
    private BigDecimal gpa;
    @JsonProperty("avatar_url")
    private String avatarUrl;
    private String phone;
    private String address;
    private Boolean gender;
    private String course;
    private String status;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
