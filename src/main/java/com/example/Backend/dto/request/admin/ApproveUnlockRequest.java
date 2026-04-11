package com.example.Backend.dto.request.admin;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ApproveUnlockRequest {
    private boolean approved; // true: Cho phép sửa, false: Từ chối
    @JsonProperty("admin_note")
    private String adminNote;
}
