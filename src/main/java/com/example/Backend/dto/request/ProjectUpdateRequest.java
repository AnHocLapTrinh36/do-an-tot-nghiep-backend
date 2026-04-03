package com.example.Backend.dto.request;
import com.example.Backend.entity.PriceType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProjectUpdateRequest {
    private Integer courseId;
    private String title;
    private String description;
    private String sourceCodeUrl;
    private String demoUrl;
    private PriceType priceType;
    private BigDecimal priceDownload;

    private List<Long> technologyIds;

    // Danh sách ID của các ảnh cũ muốn giữ lại
    private List<Long> retainedImageIds;

    // Các file ảnh upload mới
    private List<MultipartFile> newImages;
}