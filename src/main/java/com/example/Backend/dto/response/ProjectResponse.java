package com.example.Backend.dto.response;
import com.example.Backend.entity.PriceType;
import com.example.Backend.entity.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProjectResponse {
    private Long id;
    @JsonProperty("student_name")
    private String studentName; // Chỉ trả về tên cho gọn
    @JsonProperty("course_name")
    private String courseName;
    private String title;
    private String description;
    @JsonProperty("source_code_url")
    private String sourceCodeUrl;
    @JsonProperty("demo_url")
    private String demoUrl;
    private ProjectStatus status;
    @JsonProperty("price_type")
    private PriceType priceType;
    @JsonProperty("price_download")
    private BigDecimal priceDownload;
    @JsonProperty("view_count")
    private Integer viewCount;
    @JsonProperty("download_count")
    private Integer downloadCount;
    @JsonProperty("is_purchased")
    private Boolean isPurchased;

    // Trả về danh sách chi tiết thay vì chỉ ID
    private List<TechnologyResponse> technologies;
    private List<ProjectImageResponse> images;

    private LocalDateTime createdAt;
}
