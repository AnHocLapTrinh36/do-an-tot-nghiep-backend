package com.example.Backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectImageResponse {
    private Long id;
    private String imageUrl;
}
