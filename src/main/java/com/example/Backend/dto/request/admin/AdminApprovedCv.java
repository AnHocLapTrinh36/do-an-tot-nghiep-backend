package com.example.Backend.dto.request.admin;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AdminApprovedCv {

    @JsonProperty("is_approved")
    private boolean isApproved;
}
