package com.example.Backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;


@Data
public class StudentUpdateRequest {

    //private BigDecimal gpa;
    private MultipartFile avatar;
    private String phone;
    private String address;
    private Boolean gender;
    private String course;
//  private String status;
//  @JsonProperty("full_name")
    @Size(min = 3, max = 100)
    private String fullName;
}
