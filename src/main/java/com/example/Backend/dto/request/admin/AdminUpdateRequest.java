package com.example.Backend.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AdminUpdateRequest {

    @JsonProperty("full_name")
    private String fullName;
    private String position;
    private String phone;
}
