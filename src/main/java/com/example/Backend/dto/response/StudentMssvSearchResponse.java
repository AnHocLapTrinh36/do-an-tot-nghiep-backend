package com.example.Backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentMssvSearchResponse {
    private Long id;

    private String mssv;

    @JsonProperty("full_name")
    private String fullName;
}
