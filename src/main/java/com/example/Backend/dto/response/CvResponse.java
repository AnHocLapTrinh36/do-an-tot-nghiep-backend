package com.example.Backend.dto.response;
import com.example.Backend.entity.CVType;
import com.example.Backend.entity.Status;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CvResponse {
    private Long id;
    @JsonProperty("student_id")
    private Long studentId;
    private String title;
    private String type;
    @JsonProperty("content_json")
    private Object contentJson; // Trả về dạng Object để React
    @JsonProperty("is_public")
    private Boolean isPublic;
    @JsonProperty("status")
    private String status;
    @JsonProperty("approved_by")
    private Long approvedBy;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    @JsonProperty("cv_file")
    private CvFileResponse cvFile;
    @JsonProperty("view_count")
    private Integer viewCount = 0;
    @JsonProperty("download_count")
    private Integer downloadCount = 0;
}