package com.example.Backend.dto.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class CvRequest {
    private String title;
    @JsonProperty("content_json")
    private Map<String, Object> contentJson;

}
