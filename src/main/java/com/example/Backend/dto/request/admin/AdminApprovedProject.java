package com.example.Backend.dto.request.admin;

import com.example.Backend.entity.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class AdminApprovedProject {

    private ProjectStatus status;
    @JsonProperty("admin_note")
    private String adminNote;
}
