package com.example.Backend.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class CompanyUpdateRequest {
    @Size(max = 255, message = "Tên công ty không được vượt quá 255 ký tự")
    private String companyName;
    @Pattern(regexp = "^\\d{13}$", message = "Mã số thuế phải đúng 13 chữ số")
    private String taxCode;
    @Size(max = 255, message = "Website không được vượt quá 255 ký tự")
    private String website;
    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String address;
    @Pattern(regexp = "^(\\d{10,13})?$", message = "Số điện thoại phải từ 10 đến 13 chữ số")
    private String phone;

    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    private String description;

    @Size(max = 255, message = "Vị trí không được vượt quá 255 ký tự")
    private String location;

    private MultipartFile avatarUrl;
}
