package com.example.Backend.dto.response;

import com.example.Backend.entity.RoleType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {

    private String email;
    @JsonProperty("full_name")
    private String fullName;
    private RoleType role;
    @JsonProperty("is_active")
    private Boolean isActive;
}