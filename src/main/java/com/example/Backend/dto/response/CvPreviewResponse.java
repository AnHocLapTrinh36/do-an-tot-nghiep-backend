package com.example.Backend.dto.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CvPreviewResponse {
    private Long id;
    private String type;
    private Object data;
}
