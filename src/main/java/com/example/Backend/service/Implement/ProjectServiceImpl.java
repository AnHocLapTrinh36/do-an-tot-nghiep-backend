package com.example.Backend.service.Implement;

import com.example.Backend.cloudinary.CloudinaryService;
import com.example.Backend.dto.request.ProjectRequest;
import com.example.Backend.dto.request.admin.AdminApprovedProject;
import com.example.Backend.dto.response.ProjectResponse;
import com.example.Backend.entity.*;
import com.example.Backend.mapper.ProjectMapper;
import com.example.Backend.repository.AdminRepository;
import com.example.Backend.repository.CourseRepository;
import com.example.Backend.repository.ProjectPurchaseRepository;
import com.example.Backend.repository.ProjectRepository;
import com.example.Backend.repository.StudentRepository;
import com.example.Backend.repository.TechnologyRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.repository.*;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.NotificationService;
import com.example.Backend.service.ProjectService;
import com.example.Backend.dto.request.ProjectUpdateRequest;
import com.example.Backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final TechnologyRepository technologyRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final ProjectMapper projectMapper;
    private final CloudinaryService cloudinaryService;
    private final AdminRepository adminRepository;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ProjectPurchaseRepository projectPurchaseRepository;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request, String email) {

        // 1. Lấy thông tin Student từ Email
        Student student = studentRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

        //Lấy thông tin Course
        Course course = null;
        if (request.getCourseId() != null) {
            course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học"));
        }

        //Lấy danh sách Technologies từ Database dựa vào IDs truyền lên
        List<Technology> technologies = technologyRepository.findAllById(request.getTechnologyIds());

        //Khởi tạo Project
        Project project = Project.builder()
                .student(student)
                .course(course)
                .title(request.getTitle())
                .description(request.getDescription())
                .sourceCodeUrl(request.getSourceCodeUrl())
                .demoUrl(request.getDemoUrl())
                .priceType(request.getPriceType())
                .priceDownload(request.getPriceDownload())
                .technologies(new HashSet<>(technologies)) // Hibernate tự động Insert vào bảng project_technologies
                .build();

        //Xử lý Upload Ảnh qua Cloudinary
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                // Gọi hàm upload nhiều ảnh, lưu vào thư mục "project_images", dùng account "1"
                List<String> uploadedImageUrls = cloudinaryService.uploadMultipleImages(request.getImages(), "project_images", "1");

                for (String imageUrl : uploadedImageUrls) {
                    ProjectImage image = ProjectImage.builder()
                            .imageUrl(imageUrl)
                            .build();
                    project.addImage(image); // Cập nhật quan hệ 2 chiều
                }
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi upload ảnh lên Cloudinary: " + e.getMessage());
            }
        }

        // Lưu xuống DB (Hibernate sẽ tự động Insert Project, Project_Images và Project_Technologies)
        Project savedProject = projectRepository.save(project);
        List<User> admins = userRepository.findByRole(RoleType.ADMIN);
        for (User admin : admins) {
            notificationService.createNotification(
                    admin.getEmail(),
                    "NEW_PROJECT_SUBMITTED",
                    "Có đồ án mới được đăng tải",
                    "Sinh viên " + student.getUser().getFullName() + " vừa đăng dự án: " + savedProject.getTitle()
            );
        }
        // Map sang Response trả về
        return projectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request, String email) {

        Student student = studentRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đồ án"));

        if (!project.getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("403 Forbidden: Bạn không có quyền sửa đồ án này");
        }

        // business Logic: Reset trạng thái nếu đang APPROVED
        if (project.getStatus() == ProjectStatus.APPROVED) {
            project.setStatus(ProjectStatus.PENDING);
        }

        // Cập nhật thông tin cơ bản
        projectMapper.updateProject(request, project);

        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học"));
            project.setCourse(course);
        }

        //Cập nhật Technologies
        if (request.getTechnologyIds() != null) {
            List<Technology> newTechnologies = technologyRepository.findAllById(request.getTechnologyIds());
            project.getTechnologies().clear();
            project.getTechnologies().addAll(newTechnologies);
        }

        //XỬ LÝ ẢNH (Bắt buộc 3 ảnh)
        int retainedCount = (request.getRetainedImageIds() != null) ? request.getRetainedImageIds().size() : 0;
        int newCount = (request.getNewImages() != null) ? request.getNewImages().size() : 0;

        // Validate số lượng ảnh phải đúng 3
        if (retainedCount + newCount != 3) {
            throw new RuntimeException("Đồ án bắt buộc phải có đúng 3 ảnh minh họa.");
        }

        //lọc và Xóa các ảnh cũ không nằm trong danh sách "retainedImageIds"
        // copy ra một list tạm (currentImages)
        List<ProjectImage> currentImages = new ArrayList<>(project.getImages());

        for (ProjectImage img : currentImages) {
            if (request.getRetainedImageIds() == null || !request.getRetainedImageIds().contains(img.getId())) {
                // Xóa file thực tế trên Cloudinary
                try {
                    String publicId = cloudinaryService.extractPublicId(img.getImageUrl());
                    cloudinaryService.deleteAvatar(publicId, "1");
                } catch (Exception e) {
                    System.err.println("Lỗi xóa ảnh Cloudinary: " + e.getMessage());
                    // tiếp tục xóa trong DB dủ Cloudinary lỗi
                }

                // Xóa record trong DB  orphanRemoval = true trong Entity Project)
                project.getImages().remove(img);
            }
        }

        //Upload các ảnh mới
        if (request.getNewImages() != null && !request.getNewImages().isEmpty()) {
            try {
                List<String> uploadedUrls = cloudinaryService.uploadMultipleImages(request.getNewImages(), "project_images", "1");
                for (String url : uploadedUrls) {
                    project.addImage(ProjectImage.builder().imageUrl(url).build());
                }
            } catch (Exception e) {
                throw new RuntimeException("Lỗi upload ảnh mới: " + e.getMessage());
            }
        }

        //Lưu thay đổi và Map response
        Project savedProject = projectRepository.save(project);
        List<User> admins = userRepository.findByRole(RoleType.ADMIN);
        for (User admin : admins) {
            notificationService.createNotification(
                    admin.getEmail(),
                    "PROJECT_UPDATED_PENDING",
                    "Đồ án đã cập nhật - Cần duyệt lại",
                    "Sinh viên " + student.getUser().getFullName() + " vừa cập nhật đồ án '" + savedProject.getTitle() + "'. Trạng thái đã chuyển về PENDING, vui lòng kiểm tra lại."
            );
        }
        return projectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đồ án để xóa"));
        if (!isAdmin) {
            // Trích xuất email của chủ sở hữu đồ án
            String ownerEmail = project.getStudent().getUser().getEmail();

            if (!ownerEmail.equals(currentUserEmail)) {
                throw new RuntimeException("403 Forbidden: Bạn không có quyền xóa đồ án của người khác");
            }
        }

        // Thực hiện Soft Delete
        project.setStatus(ProjectStatus.CLOSE);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public void updateProjectByAdmin(Long projectId, AdminApprovedProject request) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đồ án"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Admin admin = adminRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin đang thao tác"));

        project.setStatus(request.getStatus());
        if (request.getAdminNote() != null && !request.getAdminNote().trim().isEmpty()) {
            project.setAdminNote(request.getAdminNote());
        } else {
            project.setAdminNote(null);
        }
        projectRepository.save(project);
        if (request.getStatus() == ProjectStatus.APPROVED) {
            auditLogService.saveLog(admin.getUser().getId(), project.getId(), "Duyệt đồ án");
            notificationService.createNotification(
                    project.getStudent().getUser().getEmail(),
                    "PROJECT_APPROVED",
                    "Đồ án của bạn đã được duyệt",
                    "Chúc mừng! đồ án" + project.getTitle() + "đã được phê duyệt và sẽ hiẻn thị công khai "
            );
        } else if (request.getStatus() == ProjectStatus.REJECTED) {
            auditLogService.saveLog(admin.getUser().getId(), project.getId(), "Từ chối đồ án");
            notificationService.createNotification(
                    project.getStudent().getUser().getEmail(),
                    "PROJECT_REJECTED",
                    "Đồ án của bạn đã bị từ chối yêu cầu phê duyệt ",
                    "Rất tiếc! đồ án" + project.getTitle() + "đã bị từ chối phê duyệt "
            );
        }
    }

    @Override
    public Page<ProjectResponse> getPublicProjects(String title, List<Long> technologyIds, int page, int size) {

        // Tạo đối tượng Pageable, phân trang và sắp xếp mới nhất lên đầu
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Gọi DB lấy dữ liệu (Chỉ lấy APPROVED)
        List<Long> filterTechnologyIds = normalizeTechnologyIds(technologyIds);

        Page<Project> projectPage;
        if (filterTechnologyIds.size() > 1) {
            projectPage = projectRepository.findPublicProjectsByTechnologyIds(
                    normalizeFilter(title),
                    filterTechnologyIds,
                    filterTechnologyIds.size(),
                    PageRequest.of(page, size)
            );
        } else {
            Long singleTechnologyId = filterTechnologyIds.isEmpty() ? null : filterTechnologyIds.get(0);
            projectPage = projectRepository.findPublicProjects(normalizeFilter(title), singleTechnologyId, pageable);
        }

        return projectPage.map(projectMapper::toResponse);
    }

    @Override
    public List<ProjectResponse> getMyProjects() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        // Truy vấn toàn bộ danh sách đồ án của sinh viên
        List<Project> myProjects = projectRepository.findByStudentEmailAndStatusNotClose(currentUserEmail);

        return myProjects.stream()
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProjectResponse> getAllProjectsForAdmin(String studentName, String mssv, ProjectStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return projectRepository.findAllProjectsForAdmin(
                normalizeFilter(studentName),
                normalizeFilter(mssv),
                status != null ? status.name() : null,
                pageable
        ).map(projectMapper::toResponse);
    }

    @Override
    public ProjectResponse getProjectDetail(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đồ án"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            if (project.getStatus() != ProjectStatus.APPROVED) {
                throw new RuntimeException("403 Forbidden: Đồ án này chưa được công khai");
            }

            project.setViewCount(project.getViewCount() + 1);
            projectRepository.save(project);
            ProjectResponse response = projectMapper.toResponse(project);
            response.setIsPurchased(false);
            return response;
        }

        String currentEmail = auth.getName();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isTeacher = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));

        String ownerEmail = project.getStudent().getUser().getEmail();

        boolean isOwner = ownerEmail.equals(currentEmail);

        boolean isTeacherOfCourse = false;
        if (isTeacher && project.getCourse() != null && project.getCourse().getTeachers() != null) {
            isTeacherOfCourse = project.getCourse().getTeachers().stream()
                    .anyMatch(teacher ->
                            teacher.getUser() != null &&
                                    teacher.getUser().getEmail() != null &&
                                    teacher.getUser().getEmail().equals(currentEmail)
                    );
        }

        if (project.getStatus() != ProjectStatus.APPROVED) {
            if (!isAdmin && !isOwner && !isTeacherOfCourse) {
                throw new RuntimeException("403 Forbidden: Bạn không có quyền xem đồ án này");
            }
        }

        project.setViewCount(project.getViewCount() + 1);
        projectRepository.save(project);

        ProjectResponse response = projectMapper.toResponse(project);
        boolean isPurchased = userRepository.findByEmail(currentEmail)
                .map(user -> projectPurchaseRepository.existsByProjectIdAndUserId(project.getId(), user.getId()))
                .orElse(false);
        response.setIsPurchased(isPurchased);

        return response;
    }

    private String normalizeFilter(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private List<Long> normalizeTechnologyIds(List<Long> technologyIds) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        if (technologyIds != null) {
            technologyIds.stream()
                    .filter(id -> id != null)
                    .forEach(ids::add);
        }
        return new ArrayList<>(ids);
    }

}
