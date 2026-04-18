package com.example.Backend.dto.request.admin;

import com.example.Backend.entity.Status;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUpdateProjectAccessRequestStatusRequest {

    @NotNull(message = "status khong duoc de trong")
    private Status status;

    @JsonProperty("approval_note")
    private String approvalNote;
}
