package com.example.Backend.dto.request.admin;


import com.example.Backend.entity.Status;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AdminApprovedCv {

    private Status status;

    @JsonProperty("admin_note")
    private String adminNote;

}
