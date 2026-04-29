package com.example.Backend.controller;

import com.example.Backend.dto.apiResponse.ApiResponse;
import com.example.Backend.dto.request.CvRequest;
import com.example.Backend.dto.request.CvUploadRequest;
import com.example.Backend.dto.request.UnlockRequest;
import com.example.Backend.dto.request.admin.AdminApprovedCv;
import com.example.Backend.dto.request.admin.ApproveUnlockRequest;
import com.example.Backend.dto.response.CvPreviewResponse;
import com.example.Backend.dto.response.CvResponse;
import com.example.Backend.dto.response.UnlockRequestResponse;
import com.example.Backend.entity.Status;
import com.example.Backend.service.CvService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<ApiResponse<List<CvResponse>>> getMyCVs() {
        List<CvResponse> response = cvService.getMyCvs();
        return ResponseEntity.ok(ApiResponse.success("CV của sinh viên hiện tại", response));
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
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN','COMPANY')")
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
    @GetMapping("/{id}/preview")
    @PreAuthorize("isAuthenticated()") // Cho phép mọi user đã đăng nhập gọi, Service sẽ check quyền chi tiết
    @Operation(summary = "Lấy nội dung CV để hiển thị preview (HTML/JSON hoặc PDF)")
    public ResponseEntity<ApiResponse<CvPreviewResponse>> previewCv(@PathVariable Long id) {
        CvPreviewResponse response = cvService.previewCv(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu preview thành công", response));
    }

    /// admin duyệt Cv của sinh viên (role admin)
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Duyệt hoặc từ chối CV (Chỉ Admin)")
    public ResponseEntity<ApiResponse<CvResponse>> approveCv(
            @PathVariable Long id,
            @RequestBody AdminApprovedCv request) {
        Status newStatus = request.getStatus();
        String adminNote = request.getAdminNote();

        if (newStatus == null) {
            throw new IllegalArgumentException("Trạng thái (status) không được để trống!");
        }
        if (newStatus == Status.REJECTED && (adminNote == null || adminNote.trim().isEmpty())) {
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối CV (admin_note)!");
        }
        CvResponse response = cvService.approveCv(id, newStatus, adminNote);
        String message = newStatus == Status.APPROVED ? "Duyệt CV thành công"
                : newStatus == Status.REJECTED ? "Từ chối CV thành công"
                : "Đã chuyển về trạng thái chờ duyệt";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @GetMapping("/public")
    @Operation(summary = "Danh sách CV public đã được duyệt (hỗ trợ search title, filter loại CV và nhiều kỹ năng/công nghệ)")
    public ResponseEntity<ApiResponse<Page<CvResponse>>> getPublicApprovedCvs(
            @RequestParam(value = "keyword", required = false) String keyword, // Tìm theo title CV
            @RequestParam(value = "type", required = false) String type,       // Lọc theo loại: FORM hoặc UPLOAD
            @RequestParam(value = "techs", required = false) List<String> techs, // Lọc theo danh sách kỹ năng (truyền mảng)
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir
    ) {
        // Xử lý trường hợp front-end gửi mảng rỗng [] thì  chuyển thành null
        // để Specification phía dưới bỏ qua điều kiện lọc này
        if (techs != null && techs.isEmpty()) {
            techs = null;
        }
        // Gọi Service
        Page<CvResponse> response = cvService.getPublicApprovedCvs(keyword, type, techs, page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách CV công khai thành công", response));
    }


    // API UPDATE TRỰC TIẾP DỮ LIỆU CV FORM (Chỉ dành cho PENDING, REJECTED)
    @Operation(summary = "Update CV bằng Form (Dành cho trạng thái PENDING hoặc REJECTED)")
    @PreAuthorize("hasRole('STUDENT')")
    @PutMapping(value = "/form/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CvResponse>> updateCvForm(
            @PathVariable Long id,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {
        try {
            CvRequest request = objectMapper.readValue(dataJson, CvRequest.class);
            CvResponse response = cvService.updateCvFromForm(id, request, avatarFile);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật CV thành công. Trạng thái hiện tại: Đang chờ duyệt", response));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Dữ liệu JSON gửi lên không hợp lệ");
        }
    }

    // API GỬI TICKET XIN SỬA CV (Dành cho APPROVED)
    @Operation(summary = "Gửi yêu cầu mở khóa sửa CV (Dành cho trạng thái APPROVED)")
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping(value = "/{id}/unlock-requests")
    public ResponseEntity<ApiResponse<Void>> requestUnlockCv(
            @PathVariable Long id,
            @RequestBody UnlockRequest unlockRequest) {

        if (unlockRequest.getReason() == null || unlockRequest.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do muốn chỉnh sửa CV!");
        }

        cvService.createCvUpdateRequest(id, unlockRequest.getReason());
        return ResponseEntity.ok(ApiResponse.success("Gửi yêu cầu mở khóa CV thành công. Vui lòng chờ Admin phê duyệt."));
    }

    @Operation(summary = "Admin duyệt hoặc từ chối yêu cầu mở khóa sửa CV")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/unlock-requests/{requestId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveUnlockRequest(
            @PathVariable Long requestId,
            @RequestBody ApproveUnlockRequest request) {

        // Validate: Nếu từ chối thì bắt buộc phải có lý do
        if (!request.isApproved() && (request.getAdminNote() == null || request.getAdminNote().trim().isEmpty())) {
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối yêu cầu (adminNote)!");
        }

        // Gọi hàm Service đã viết ở bài trước
        cvService.approveUnlockRequest(requestId, request.isApproved(), request.getAdminNote());

        String message = request.isApproved()
                ? "Đã chấp nhận yêu cầu. CV đã được chuyển về trạng thái Đang chờ duyệt (PENDING) để sinh viên cập nhật."
                : "Đã từ chối yêu cầu mở khóa CV của sinh viên.";

        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @Operation(summary = "Admin lấy danh sách yêu cầu mở khóa CV")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/unlock-requests/pending")
    public ResponseEntity<ApiResponse<Page<UnlockRequestResponse>>> getPendingUnlockRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<UnlockRequestResponse> response = cvService.getPendingUnlockRequests(page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành công", response));
    }
}
