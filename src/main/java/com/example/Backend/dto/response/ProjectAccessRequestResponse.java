package com.example.Backend.dto.response;

import com.example.Backend.entity.Status;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectAccessRequestResponse {

    private Long id;

    @JsonProperty("company_id")
    private Long companyId;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("company_email")
    private String companyEmail;

    @JsonProperty("company_phone")
    private String companyPhone;

    @JsonProperty("company_website")
    private String companyWebsite;

    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("project_title")
    private String projectTitle;

    @JsonProperty("student_name")
    private String studentName;

    private String reason;
    private Status status;

    @JsonProperty("requested_at")
    private LocalDateTime requestedAt;

    @JsonProperty("processed_at")
    private LocalDateTime processedAt;

    @JsonProperty("approval_note")
    private String approvalNote;
}
