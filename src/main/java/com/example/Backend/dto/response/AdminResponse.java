package com.example.Backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminResponse {

    private Long id;
    private String email;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("admin_code")
    private String adminCode;
    private String position;
    private String phone;
}
