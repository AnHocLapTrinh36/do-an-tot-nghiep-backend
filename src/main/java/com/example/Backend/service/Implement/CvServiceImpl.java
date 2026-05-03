package com.example.Backend.service.Implement;

import com.example.Backend.cloudinary.CloudinaryService;
import com.example.Backend.config.CvSpecification;
import com.example.Backend.dto.request.CvRequest;
import com.example.Backend.dto.request.CvUploadRequest;
import com.example.Backend.dto.response.CvFileResponse;
import com.example.Backend.dto.response.CvPreviewResponse;
import com.example.Backend.dto.response.CvResponse;
import com.example.Backend.dto.response.UnlockRequestResponse;
import com.example.Backend.entity.*;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.repository.*;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.CvService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CvServiceImpl implements CvService {

    private final CvRepository cvRepository;
    private final CloudinaryService cloudinaryService;
    private final ObjectMapper objectMapper;
    private final StudentRepository studentRepository;
    private final CvFileRepository cvFileRepository;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CvUpdateRequestRepository cvUpdateRequestRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public CvResponse createCvFromForm(CvRequest request, MultipartFile avatarFile) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("email: {}", email);

        Student student = studentRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Kiểm tra giới hạn 2 CV
        long currentCvCount = cvRepository.countByStudentId(student.getId());
        if (currentCvCount >= 2) {
            throw new RuntimeException("Mỗi sinh viên chỉ được tạo tối đa 2 CV. Bạn đã có "
                    + currentCvCount + " CV.");
        }

        Map<String, Object> contentMap = request.getContentJson();

        // Upload avatar nếu có
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String newAvatarUrl = cloudinaryService.uploadAvatar(avatarFile, "avatars/cv", "1");
                if (newAvatarUrl != null) {
                    contentMap.put("avatar", newAvatarUrl);
                }
            } catch (Exception e) {
                log.error("Failed to upload CV avatar", e);
                throw new RuntimeException("Không thể upload avatar cho CV");
            }
        }

        Cv cv = new Cv();
        cv.setStudent(student);
        cv.setTitle(request.getTitle());
        cv.setType(CVType.FORM);
        cv.setIsPublic(false);
        cv.setStatus(Status.PENDING); // Set Status bằng Enum

        try {
            String jsonString = objectMapper.writeValueAsString(contentMap);
            cv.setContentJson(jsonString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi định dạng dữ liệu CV");
        }

        Cv savedCv = cvRepository.save(cv);
        return toResponse(savedCv);
    }

    @Override
    @Transactional
    public CvResponse uploadCv(CvUploadRequest request, MultipartFile file) throws IOException {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Student student = studentRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên"));

        // Kiểm tra giới hạn tối đa 2 CV
        if (cvRepository.countByStudentId(student.getId()) >= 2) {
            throw new BadRequestException("Mỗi sinh viên chỉ được tạo tối đa 2 CV. Bạn đã có "
                    + cvRepository.countByStudentId(student.getId()) + " CV.");
        }

        // Kiểm tra file PDF
        if (file == null || file.isEmpty() ||
                !file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new BadRequestException("Chỉ chấp nhận file PDF");
        }

        // Upload file lên Cloudinary
        String fileUrl = cloudinaryService.uploadFile(file, "cvs", "1");

        // Tạo CV
        Cv cv = new Cv();
        cv.setStudent(student);
        cv.setTitle(request.getTitle() != null && !request.getTitle().trim().isEmpty()
                ? request.getTitle().trim()
                : "CV Upload PDF");
        cv.setType(CVType.UPLOAD);
        cv.setContentJson(null);
        cv.setIsPublic(false);
        cv.setStatus(Status.PENDING); // Set Status bằng Enum
        cv.setCreatedAt(LocalDateTime.now());
        cv.setUpdatedAt(LocalDateTime.now());

        // Lưu CV trước
        Cv savedCv = cvRepository.save(cv);

        // Lưu thông tin file PDF
        CvFile cvFile = new CvFile();
        cvFile.setCv(savedCv);
        cvFile.setFileName(file.getOriginalFilename());
        cvFile.setFilePath(fileUrl);
        cvFile.setFileSize((int) file.getSize());
        cvFile.setUploadedAt(LocalDateTime.now());
        cvFileRepository.save(cvFile);

        // Refresh entity để lấy đầy đủ relation (cvFile)
        savedCv = cvRepository.findById(savedCv.getId()).orElse(savedCv);

        return toResponse(savedCv);
    }

    @Override
    @Transactional
    public CvResponse updateCvFromForm(Long cvId, CvRequest request, MultipartFile avatarFile) {
        // Lấy thông tin sinh viên từ token
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ sinh viên"));

        // Tìm CV thuộc về sinh viên này
        Cv cv = cvRepository.findByIdAndStudentId(cvId, student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy CV hoặc bạn không có quyền chỉnh sửa"));

        // XỬ LÝ LOGIC CHẶN NẾU CV ĐÃ APPROVED
        if (cv.getStatus() == Status.APPROVED) {
            throw new RuntimeException("CV này đã được duyệt. Vui lòng gửi yêu cầu mở khóa (Ticket) thay vì lưu trực tiếp!");
        }

        // Xử lý update JSON cho PENDING và REJECTED
        Map<String, Object> newContentMap = request.getContentJson();

        // Xử lý avatar
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                // Thử lấy avatar cũ để xóa
                if (cv.getContentJson() != null && !cv.getContentJson().isEmpty()) {
                    Map<String, Object> oldContentMap = objectMapper.readValue(cv.getContentJson(), Map.class);
                    String oldAvatarUrl = (String) oldContentMap.get("avatar");
                    if (oldAvatarUrl != null && oldAvatarUrl.contains("cloudinary.com")) {
                        String oldPublicId = cloudinaryService.extractPublicId(oldAvatarUrl);
                        if (oldPublicId != null) {
                            cloudinaryService.deleteAvatar(oldPublicId, "1");
                        }
                    }
                }

                // Up avatar mới
                String newAvatarUrl = cloudinaryService.uploadAvatar(avatarFile, "avatars/cv", "1");
                if (newAvatarUrl != null) {
                    newContentMap.put("avatar", newAvatarUrl);
                }
            } catch (Exception e) {
                log.error("Update avatar failed", e);
                throw new RuntimeException("Lỗi cập nhật ảnh đại diện");
            }
        }

        cv.setTitle(request.getTitle());

        //Reset lại thông tin khi update (dù trước đó là PENDING hay REJECTED)
        cv.setStatus(Status.PENDING);
        cv.setIsPublic(false);
        cv.setApprovedBy(null);
        cv.setAdminNote(null); // Xóa lý do từ chối cũ (nếu có)

        try {
            cv.setContentJson(objectMapper.writeValueAsString(newContentMap));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi định dạng dữ liệu CV");
        }

        Cv updatedCv = cvRepository.save(cv);
        return toResponse(updatedCv);
    }


    @Override
    @Transactional
    public void createCvUpdateRequest(Long cvId, String reason) {
        // Xác thực sinh viên
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ sinh viên"));

        // Tìm CV
        Cv cv = cvRepository.findByIdAndStudentId(cvId, student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy CV hoặc bạn không có quyền"));

        // Kiểm tra CV có đang ở trạng thái APPROVED không
        if (cv.getStatus() != Status.APPROVED) {
            throw new RuntimeException("CV của bạn đang ở trạng thái " + cv.getStatus() + ". Bạn có thể sửa trực tiếp mà không cần gửi yêu cầu!");
        }

        // Kiểm tra xem có ticket nào đang Pending không (tránh spam)
        boolean hasPendingRequest = cvUpdateRequestRepository.existsByCvIdAndStatus(cvId, Status.PENDING);
        if (hasPendingRequest) {
            throw new RuntimeException("Bạn đã gửi một yêu cầu mở khóa cho CV này và đang chờ Admin duyệt.");
        }

        //Lưu Ticket
        CvUpdateRequest updateRequest = CvUpdateRequest.builder()
                .cv(cv)
                .student(student)
                .reason(reason)
                .status(Status.PENDING)
                .build();

        cvUpdateRequestRepository.save(updateRequest);
    }

    /// admin duyệt ticket
    @Transactional
    public void approveUnlockRequest(Long requestId, boolean isApproved, String adminNote) {
        // Lấy ticket ra
        CvUpdateRequest request = cvUpdateRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu mở khóa"));

        //Nếu Admin duyệt (Cho phép sửa)
        if (isApproved) {
            request.setStatus(Status.APPROVED); // Cập nhật ticket

            // QUAN TRỌNG NHẤT: Đổi trạng thái CV gốc về PENDING để sinh viên có thể sửa
            Cv cv = request.getCv();
            cv.setStatus(Status.PENDING);
            cv.setIsPublic(false); // Gỡ khỏi trang public
            cv.setApprovedBy(null);
            cvRepository.save(cv);
        }
        // Nếu Admin từ chối yêu cầu mở khóa
        else {
            request.setStatus(Status.REJECTED);
            request.setAdminNote(adminNote);
            // CV gốc giữ nguyên là APPROVED, không thay đổi gì cả
        }

        cvUpdateRequestRepository.save(request);
    }

    /// danh sách cv edit cần phê duyệt
    @Override
    public Page<UnlockRequestResponse> getPendingUnlockRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestedAt").descending());

        // Chỉ lấy những yêu cầu đang chờ duyệt (PENDING)
        Page<CvUpdateRequest> requests = cvUpdateRequestRepository.findByStatus(Status.PENDING, pageable);

        return requests.map(req -> UnlockRequestResponse.builder()
                .id(req.getId())
                .cvId(req.getCv().getId())
                .cvTitle(req.getCv().getTitle())
                .studentId(req.getStudent().getId())
                .studentName(req.getStudent().getUser().getFullName())
                .mssv(req.getStudent().getMssv())
                .reason(req.getReason())
                .status(req.getStatus().name())
                .requestedAt(req.getRequestedAt())
                .build());
    }

    @Override
    public List<CvResponse> getMyCvs() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Student student = studentRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + email));

        List<Cv> cvs = cvRepository.findByStudentId(student.getId());

        return cvs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CvResponse> getAllCvs() {
        List<Cv> cvs = cvRepository.findAllByOrderByCreatedAtDesc();

        return cvs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CvResponse getCvById(Long id) {
        Cv cv = cvRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy CV với ID: " + id));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        // Nếu là ADMIN -> Cho phép xem ngay lập tức
        if (isAdmin) return toResponse(cv);

        // 2. Nếu là COMPANY -> Kiểm tra xem sinh viên có nộp CV này cho mình không
        boolean isCompany = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_COMPANY"));

        if (isCompany) {
            // Tìm công ty hiện tại dựa trên email đăng nhập
            Company currentCompany = companyRepository.findByUserEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ công ty"));
            boolean hasApplied = jobApplicationRepository.existsByCv_IdAndJobPost_Company_Id(id, currentCompany.getId());

            if (!hasApplied) {
                throw new RuntimeException("Bạn không có quyền xem CV này vì ứng viên chưa nộp cho công ty bạn.");
            }
            return toResponse(cv);
        }
        Student currentStudent = studentRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ sinh viên của user hiện tại"));

        if (!cv.getStudent().getId().equals(currentStudent.getId())) {
            throw new RuntimeException("Bạn không có quyền xem CV này vì đây không phải là CV của bạn");
        }
        return toResponse(cv);
    }

    @Override
    @Transactional
    public void deleteCv(Long id) {
        Cv cv = cvRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy CV với ID: " + id));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            Student currentStudent = studentRepository.findByUser_Email(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ sinh viên"));
            if (!cv.getStudent().getId().equals(currentStudent.getId())) {
                throw new RuntimeException("Bạn không có quyền xóa CV này");
            }
        }
        cvRepository.delete(cv);
    }

    @Override
    @Transactional
    public CvResponse approveCv(Long cvId, Status status, String adminNote) {
        //Lấy thông tin CV cần duyệt
        Cv cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy CV với ID: " + cvId));

        // Lấy thông tin Admin đang đăng nhập từ Security Context
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Admin currentAdmin = adminRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin Admin thao tác"));

        // Cập nhật trạng thái bằng Enum
        cv.setStatus(status);

        if (status == Status.APPROVED) {
            // Nếu duyệt -> Cập nhật người duyệt và bật public cho CV
            cv.setApprovedBy(currentAdmin.getId());
            cv.setIsPublic(true);
            cv.setAdminNote(null);
            auditLogService.saveLog(currentAdmin.getUser().getId(), cv.getId(), "Duyệt CV");
        } else {
            // Từ chối hoặc trả về Pending thì gỡ Public
            cv.setApprovedBy(null);
            cv.setIsPublic(false);
            cv.setAdminNote(adminNote);
            if (status == Status.REJECTED) {
                auditLogService.saveLog(currentAdmin.getUser().getId(), cv.getId(), "Từ chối CV");
            }
        }

        Cv updatedCv = cvRepository.save(cv);
        return toResponse(updatedCv);
    }

    @Override
    @Transactional
    public CvPreviewResponse previewCv(Long id) {
        Cv cv = cvRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy CV với ID: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        // Phân quyền xem Preview
        if (!isAdmin) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

            if (user.getRole() == RoleType.STUDENT) {
                if (!cv.getStudent().getUser().getEmail().equals(email)) {
                    // Không phải chủ sở hữu -> Chỉ xem được nếu CV đã public và approved
                    if (!cv.getIsPublic() || cv.getStatus() != Status.APPROVED) {
                        throw new RuntimeException("Bạn không có quyền xem CV này");
                    }
                }
            } else if (user.getRole() == RoleType.COMPANY) {
                if (!cv.getIsPublic() || cv.getStatus() != Status.APPROVED) {
                    throw new RuntimeException("CV này chưa được công khai");
                }
            }
        }

        cvRepository.save(cv); // Lưu ViewCount nếu có logic tăng view

        Object previewData = null;

        if (cv.getType() == CVType.FORM && cv.getContentJson() != null) {
            try {
                previewData = objectMapper.readValue(cv.getContentJson(), Object.class);
            } catch (Exception e) {
                log.error("Lỗi parse JSON content của CV ID: {}", id, e);
                throw new RuntimeException("Dữ liệu CV Form bị lỗi định dạng");
            }
        } else if (cv.getType() == CVType.UPLOAD && cv.getCvFile() != null) {
            previewData = cv.getCvFile().getFilePath();
        }

        return CvPreviewResponse.builder()
                .id(cv.getId())
                .type(cv.getType().name())
                .data(previewData)
                .build();
    }

    @Override
    public Page<CvResponse> getPublicApprovedCvs(String keyword, String type, List<String> techs, int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Cv> spec = CvSpecification.filterPublicApprovedCvs(keyword, type, techs);

        return cvRepository.findAll(spec, pageable)
                .map(this::toResponse);
    }


    /**
     * Hàm chuyển đổi chung Cv → CvResponse
     */
    private CvResponse toResponse(Cv cv) {
        CvFileResponse cvFileResponse = null;
        if (CVType.UPLOAD.equals(cv.getType()) && cv.getCvFile() != null) {
            CvFile file = cv.getCvFile();
            cvFileResponse = CvFileResponse.builder()
                    .id(file.getId())
                    .fileName(file.getFileName())
                    .filePath(file.getFilePath())
                    .fileSize(file.getFileSize())
                    .build();
        }

        Object contentJsonObj = null;
        if (cv.getContentJson() != null && !cv.getContentJson().isBlank()) {
            try {
                contentJsonObj = objectMapper.readValue(cv.getContentJson(), Object.class);
            } catch (Exception e) {
                log.error("Failed to parse content_json for CV ID {}", cv.getId(), e);
            }
        }

        boolean hasPending = cvUpdateRequestRepository.existsByCvIdAndStatus(cv.getId(), Status.PENDING);

        return CvResponse.builder()
                .id(cv.getId())
                .studentId(cv.getStudent() != null ? cv.getStudent().getId() : null)
                .title(cv.getTitle())
                .type(cv.getType().name())
                .contentJson(contentJsonObj)
                .isPublic(cv.getIsPublic())
                .status(cv.getStatus().name()) // Trả về Status thay vì isApproved
                .approvedBy(cv.getApprovedBy())
                .createdAt(cv.getCreatedAt())
                .updatedAt(cv.getUpdatedAt())
                .adminNote(cv.getAdminNote())
                .cvFile(cvFileResponse)
                .downloadCount(cv.getDownloadCount())
                .viewCount(cv.getViewCount())
                .hasPendingRequest(hasPending)
                .build();
    }
}
