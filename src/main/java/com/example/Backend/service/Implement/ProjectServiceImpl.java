package com.example.Backend.service.Implement;

import com.example.Backend.cloudinary.CloudinaryService;
import com.example.Backend.dto.request.ProjectRequest;
import com.example.Backend.dto.request.ProjectUpdateRequest;
import com.example.Backend.dto.request.admin.AdminApprovedProject;
import com.example.Backend.dto.response.ProjectResponse;
import com.example.Backend.entity.Admin;
import com.example.Backend.entity.Course;
import com.example.Backend.entity.Project;
import com.example.Backend.entity.ProjectImage;
import com.example.Backend.entity.ProjectMember;
import com.example.Backend.entity.ProjectStatus;
import com.example.Backend.entity.RoleType;
import com.example.Backend.entity.Student;
import com.example.Backend.entity.Technology;
import com.example.Backend.entity.User;
import com.example.Backend.mapper.ProjectMapper;
import com.example.Backend.repository.AdminRepository;
import com.example.Backend.repository.CourseRepository;
import com.example.Backend.repository.ProjectMemberRepository;
import com.example.Backend.repository.ProjectPurchaseRepository;
import com.example.Backend.repository.ProjectRepository;
import com.example.Backend.repository.StudentRepository;
import com.example.Backend.repository.TechnologyRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.NotificationService;
import com.example.Backend.service.ProjectService;
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
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request, String email) {
        Student student = studentRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

        Course course = null;
        if (request.getCourseId() != null) {
            course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học"));
        }

        List<Technology> technologies = request.getTechnologyIds() == null
                ? List.of()
                : technologyRepository.findAllById(request.getTechnologyIds());

        Project project = Project.builder()
                .student(student)
                .course(course)
                .title(request.getTitle())
                .description(request.getDescription())
                .sourceCodeUrl(request.getSourceCodeUrl())
                .demoUrl(request.getDemoUrl())
                .priceType(request.getPriceType())
                .priceDownload(request.getPriceDownload())
                .technologies(new HashSet<>(technologies))
                .build();

        syncProjectMembers(project, student, request.getMemberMssvs());

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                List<String> uploadedImageUrls = cloudinaryService.uploadMultipleImages(
                        request.getImages(),
                        "project_images",
                        "1"
                );

                for (String imageUrl : uploadedImageUrls) {
                    ProjectImage image = ProjectImage.builder()
                            .imageUrl(imageUrl)
                            .build();
                    project.addImage(image);
                }
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi upload ảnh lên Cloudinary: " + e.getMessage());
            }
        }

        Project savedProject = projectRepository.save(project);
        List<User> admins = userRepository.findByRole(RoleType.ADMIN);
        for (User admin : admins) {
            notificationService.createNotification(
                    admin.getEmail(),
                    "NEW_PROJECT_SUBMITTED",
                    "Có dự án mới được đăng tải",
                    "Sinh viên " + student.getUser().getFullName() + " vừa đăng dự án: " + savedProject.getTitle()
            );
        }

        return projectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request, String email) {
        Student student = studentRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dự án"));

        if (!project.getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("Bạn không có quyền sửa dự án này");
        }

        if (project.getStatus() == ProjectStatus.APPROVED) {
            project.setStatus(ProjectStatus.PENDING);
        }

        projectMapper.updateProject(request, project);

        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học"));
            project.setCourse(course);
        }

        if (request.getTechnologyIds() != null) {
            List<Technology> newTechnologies = technologyRepository.findAllById(request.getTechnologyIds());
            project.getTechnologies().clear();
            project.getTechnologies().addAll(newTechnologies);
        }

        int retainedCount = request.getRetainedImageIds() != null ? request.getRetainedImageIds().size() : 0;
        int newCount = request.getNewImages() != null ? request.getNewImages().size() : 0;

        if (retainedCount + newCount != 3) {
            throw new RuntimeException("Dự án bắt buộc phải có đúng 3 ảnh minh họa.");
        }

        List<ProjectImage> currentImages = new ArrayList<>(project.getImages());

        for (ProjectImage img : currentImages) {
            if (request.getRetainedImageIds() == null || !request.getRetainedImageIds().contains(img.getId())) {
                try {
                    String publicId = cloudinaryService.extractPublicId(img.getImageUrl());
                    cloudinaryService.deleteAvatar(publicId, "1");
                } catch (Exception e) {
                    System.err.println("Lỗi xóa ảnh Cloudinary: " + e.getMessage());
                }

                project.getImages().remove(img);
            }
        }

        if (request.getNewImages() != null && !request.getNewImages().isEmpty()) {
            try {
                List<String> uploadedUrls = cloudinaryService.uploadMultipleImages(
                        request.getNewImages(),
                        "project_images",
                        "1"
                );
                for (String url : uploadedUrls) {
                    project.addImage(ProjectImage.builder().imageUrl(url).build());
                }
            } catch (Exception e) {
                throw new RuntimeException("Lỗi upload ảnh mới: " + e.getMessage());
            }
        }

        Project savedProject = projectRepository.save(project);
        List<User> admins = userRepository.findByRole(RoleType.ADMIN);
        for (User admin : admins) {
            notificationService.createNotification(
                    admin.getEmail(),
                    "PROJECT_UPDATED_PENDING",
                    "Dự án đã cập nhật - cần duyệt lại",
                    "Sinh viên " + student.getUser().getFullName()
                            + " vừa cập nhật dự án '" + savedProject.getTitle()
                            + "'. Trạng thái đã chuyển về PENDING, vui lòng kiểm tra lại."
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dự án để xóa"));

        if (!isAdmin) {
            Student student = studentRepository.findByUser_Email(currentUserEmail)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

            if (!project.getStudent().getId().equals(student.getId())) {
                throw new RuntimeException("Bạn không có quyền xóa dự án của người khác");
            }
        }

        project.setStatus(ProjectStatus.CLOSE);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public void updateProjectByAdmin(Long projectId, AdminApprovedProject request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dự án"));

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
            auditLogService.saveLog(admin.getUser().getId(), project.getId(), "Duyệt dự án");
            notificationService.createNotification(
                    project.getStudent().getUser().getEmail(),
                    "PROJECT_APPROVED",
                    "Dự án của bạn đã được duyệt",
                    "Chúc mừng! Dự án " + project.getTitle() + " đã được phê duyệt và sẽ hiển thị công khai"
            );
        } else if (request.getStatus() == ProjectStatus.REJECTED) {
            auditLogService.saveLog(admin.getUser().getId(), project.getId(), "Từ chối dự án");
            notificationService.createNotification(
                    project.getStudent().getUser().getEmail(),
                    "PROJECT_REJECTED",
                    "Dự án của bạn đã bị từ chối yêu cầu phê duyệt",
                    "Rất tiếc! Dự án " + project.getTitle() + " đã bị từ chối phê duyệt"
            );
        }
    }

    @Override
    public Page<ProjectResponse> getPublicProjects(String title, List<Long> technologyIds, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
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
        List<Project> myProjects = projectRepository.findByOwnerOrMemberEmailAndStatusNotClose(currentUserEmail);

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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dự án"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            if (project.getStatus() != ProjectStatus.APPROVED) {
                throw new RuntimeException("Dự án này chưa được công khai");
            }

            incrementViewCountIfApproved(project);
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
        boolean isProjectMember = projectMemberRepository.existsByProjectIdAndStudentUserEmail(project.getId(), currentEmail);

        boolean isTeacherOfCourse = false;
        if (isTeacher && project.getCourse() != null && project.getCourse().getTeachers() != null) {
            isTeacherOfCourse = project.getCourse().getTeachers().stream()
                    .anyMatch(teacher ->
                            teacher.getUser() != null
                                    && teacher.getUser().getEmail() != null
                                    && teacher.getUser().getEmail().equals(currentEmail)
                    );
        }

        if (project.getStatus() != ProjectStatus.APPROVED) {
            if (!isAdmin && !isOwner && !isProjectMember && !isTeacherOfCourse) {
                throw new RuntimeException("Bạn không có quyền xem dự án này");
            }
        }

        incrementViewCountIfApproved(project);

        ProjectResponse response = projectMapper.toResponse(project);
        boolean isPurchased = userRepository.findByEmail(currentEmail)
                .map(user -> projectPurchaseRepository.existsByProjectIdAndUserId(project.getId(), user.getId()))
                .orElse(false);
        response.setIsPurchased(isPurchased);

        return response;
    }

    private void incrementViewCountIfApproved(Project project) {
        if (project.getStatus() != ProjectStatus.APPROVED) {
            return;
        }

        int currentViewCount = project.getViewCount() == null ? 0 : project.getViewCount();
        project.setViewCount(currentViewCount + 1);
        projectRepository.save(project);
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

    private void syncProjectMembers(Project project, Student creator, List<String> memberMssvs) {
        LinkedHashSet<String> normalizedMssvs = new LinkedHashSet<>();
        normalizedMssvs.add(creator.getMssv());

        if (memberMssvs != null) {
            for (String mssv : memberMssvs) {
                if (mssv == null || mssv.trim().isEmpty()) {
                    throw new RuntimeException("MSSV thành viên không được để trống");
                }

                String normalizedMssv = mssv.trim();
                if (!normalizedMssvs.add(normalizedMssv)) {
                    throw new RuntimeException("Danh sách thành viên bị trùng MSSV: " + normalizedMssv);
                }
            }
        }

        for (String mssv : normalizedMssvs) {
            Student member = studentRepository.findByMssv(mssv)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên với MSSV: " + mssv));

            boolean alreadyExists = project.getMembers().stream()
                    .anyMatch(projectMember -> projectMember.getStudent().getId().equals(member.getId()));
            if (!alreadyExists) {
                ProjectMember projectMember = ProjectMember.builder()
                        .student(member)
                        .build();
                project.addMember(projectMember);
            }
        }

        project.getMembers().removeIf(projectMember ->
                !normalizedMssvs.contains(projectMember.getStudent().getMssv())
        );
    }
}
