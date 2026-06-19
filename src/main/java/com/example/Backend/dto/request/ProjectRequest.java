package com.example.Backend.dto.request;

import com.example.Backend.entity.PriceType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProjectRequest {
    private Integer courseId;
    private String title;
    private String description;
    private String sourceCodeUrl;
    private String demoUrl;
    private PriceType priceType;
    private BigDecimal priceDownload;

    // Gửi mảng ID công nghệ
    private List<Long> technologyIds;

    private List<String> memberMssvs;

    // Gửi mảng Link ảnh (Có thể đổi sang MultipartFile nếu bạn muốn upload file trực tiếp)
    private List<MultipartFile> images;
}
