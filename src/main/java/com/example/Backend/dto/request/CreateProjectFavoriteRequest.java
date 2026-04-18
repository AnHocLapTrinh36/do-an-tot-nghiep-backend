package com.example.Backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProjectFavoriteRequest {

    @NotNull(message = "project_id khong duoc de trong")
    @JsonProperty("project_id")
    private Long projectId;
}
