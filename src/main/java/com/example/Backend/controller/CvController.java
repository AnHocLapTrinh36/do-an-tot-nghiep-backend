package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.CvRequest;
import com.example.Backend.dto.request.CvUploadRequest;
import com.example.Backend.dto.response.CvResponse;
import com.example.Backend.service.CvService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/cvs")
@RequiredArgsConstructor
@Slf4j
public class CvController {

    private final CvService cvService;
    private final ObjectMapper objectMapper;

    //chỉ student
    //tạo cv bằng form
    @Operation(summary = "tạo cv bằng form")
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping(value = "/form", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CvResponse> createCvForm(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {
        try {
            CvRequest request = objectMapper.readValue(dataJson, CvRequest.class);
            log.info("CvRequest: {}", objectMapper.writeValueAsString(request));
            CvResponse response = cvService.createCvFromForm(request, avatarFile);
            return ResponseEntity.ok(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Dữ liệu JSON gửi lên không hợp lệ");
        }
    }

    @PostMapping("/upload")
    @Operation(summary = "upload file cv")
    public ResponseEntity<CvResponse> uploadCv(
            @RequestPart("title") String title,
            @RequestPart("file") MultipartFile file) throws Exception {
        CvUploadRequest request = new CvUploadRequest();
        request.setTitle(title);
        CvResponse response = cvService.uploadCv(request, file);
        return ResponseEntity.ok(response);
    }

    //danh sách cv của sinh viên hiện tại, role chỉ sinh viên
    @GetMapping("/my-cvs")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "lấy danh danh sách cv của sinh viên hiện tại")
    public ResponseEntity<List<CvResponse>> getMyCVs() {
        List<CvResponse> response = cvService.getMyCvs();
        return ResponseEntity.ok(response);
    }
    //danh sách tất cả CV (role admin)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "danh sách tất cả CV (role admin)")
    public ResponseEntity<List<CvResponse>> getAllCvs() {
        List<CvResponse> responses = cvService.getAllCvs();
        return ResponseEntity.ok(responses);
    }
    /// chi tiết 1 cv
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Xem chi tiết một CV theo ID")
    public ResponseEntity<CvResponse> getCvById(@PathVariable Long id) {
        CvResponse response = cvService.getCvById(id);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Xóa CV (chỉ chủ sở hữu hoặc admin)")
    public ResponseEntity<ApiResponse<Void>> deleteCv(@PathVariable Long id) {
        cvService.deleteCv(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa CV thành công"));
    }
    /// /api/cvs/{id}/preview	Lấy nội dung CV đã render (HTML hoặc JSON) để hiển thị preview.
    ///admin duyệt Cv của sinh viên (role admin) - chờ module admin

}
