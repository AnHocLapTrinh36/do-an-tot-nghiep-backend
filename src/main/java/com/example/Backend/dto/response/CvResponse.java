package com.example.Backend.dto.response;
import com.example.Backend.entity.CVType;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("is_approved")
    private Boolean isApproved;
    @JsonProperty("approved_by")
    private Long approvedBy;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    // đối tượng chứa thông tin file (sẽ null nếu type = FORM)
    @JsonProperty("cv_file")
    private CvFileResponse cvFile;
}