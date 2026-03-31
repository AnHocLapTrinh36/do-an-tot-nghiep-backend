package com.example.Backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyResponse {
    private Long id;
    private String companyName;
    private String taxCode;
    private String website;
    private String address;
    private String phone;
    private String description;
    private String location;
    private String avatarUrl;
    private String status;
    private Boolean isVerified;
    private String email;
}
