package com.example.Backend.service.Implement;

import com.example.Backend.cloudinary.CloudinaryService;
import com.example.Backend.dto.request.JobRequest;
import com.example.Backend.dto.response.JobResponse;
import com.example.Backend.entity.*;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.mapper.JobMapper;
import com.example.Backend.repository.*;
import com.example.Backend.service.AuditLogService;
import com.example.Backend.service.JobPostService;
import com.example.Backend.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobPostServiceImp implements JobPostService {

    private final CompanyRepository companyReponsitory;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final JobMapper jobMapper;
    private final JobPostRepository jobPostRepository;
    private final ObjectMapper objectMapper;
    private final JobFavoriteRepository favoriteRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    private String convertTagsToJson(List<String> tags) {
        if (tags == null || tags.isEmpty()) return null;
        try {
            //chuyển mảng sang json
            return objectMapper.writeValueAsString(tags);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Lỗi convert tags sang JSON: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public JobResponse createJob(JobRequest request, String email) {
        Company company = companyReponsitory.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy doanh nghiệp"));
        //chi cho phep dang tin neu cong ty duoc duyet
        if (!"APPROVED".equals(company.getStatus().toString())) {
            throw new RuntimeException("Tài khoản doanh nghiệp chưa được xác thực");
        }
        JobPost job = JobPost.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .salary(request.getSalary())
                .startDay(request.getStartDay())
                .endDay(request.getEndDay())
                .company(company)
                .jobType(request.getJobType())
                .workLocation(request.getWorkLocation())
                .postedAt(LocalDateTime.now())
                .status(JobStatus.PENDING)
                .tags(convertTagsToJson(request.getTags()))
                .build();
        handleJdFileUpload(request, job);
        JobPost savedJob = jobPostRepository.save(job);
        List<User> admins = userRepository.findByRole(RoleType.ADMIN);
        for (User admin : admins) {
            notificationService.createNotification(
                    admin.getEmail(),
                    "NEW_JOB_POST",
                    "Yêu cầu duyệt tin đăng mới",
                    "Doanh nghiệp " + company.getCompanyName() + " vừa đăng tin: " + job.getTitle()
            );
        }
        return jobMapper.toResponse(savedJob);
    }

    @Override
    @Transactional
    public JobResponse updateJob(Long id, JobRequest request, String email) {

        JobPost job = jobPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng"));
        if (!job.getCompany().getUser().getEmail().equals(email)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa tin tuyển dụng này!");
        }
        //nếu tin đã được duyêt mà sửa nội dung -> Bắt admin duyệt lại
        boolean ok = false;
        if (job.getStatus() == JobStatus.APPROVED || job.getStatus() == JobStatus.REJECTED) {
            job.setStatus(JobStatus.PENDING);
            ok = true;
        }
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setSalary(request.getSalary());
        job.setStartDay(request.getStartDay());
        job.setEndDay(request.getEndDay());
        job.setJobType(request.getJobType());
        job.setWorkLocation(request.getWorkLocation());
        job.setTags(convertTagsToJson(request.getTags()));
        handleJdFileUpload(request, job);
        JobPost savedJob = jobPostRepository.save(job);


        if (ok) {
            List<User> admins = userRepository.findByRole(RoleType.ADMIN);
            for (User admin : admins) {
                notificationService.createNotification(
                        admin.getEmail(),
                        "JOB_UPDATED_PENDING",
                        "Tin tuyển dụng đã sửa - Chờ duyệt lại",
                        "Doanh nghiệp " + job.getCompany().getCompanyName() + " đã cập nhật nội dung tin '" + job.getTitle() + "'. Vui lòng kiểm tra lại."
                );
            }
        }

        return jobMapper.toResponse(savedJob);
    }

    @Override
    @Transactional
    public void closeJob(Long id) {
        JobPost job = jobPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng"));
        job.setStatus(JobStatus.CLOSE);
        jobPostRepository.save(job);
    }

    @Override
    @Transactional
    public List<JobResponse> getPendingJobsForAdmin() {
        List<JobPost> pendingJobs = jobPostRepository.findByStatus(JobStatus.PENDING);
        return jobMapper.toResponseList(pendingJobs);
    }

    @Override
    @Transactional
    public JobResponse approveJob(Long id, String action, String adminEmail) {
        boolean ok = false;
        JobPost job = jobPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin"));
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy admin thực hiện"));
        //kiem tra xem phai Approve khonh
        if ("APPROVE".equalsIgnoreCase(action.trim())) {
            job.setStatus(JobStatus.APPROVED);
            auditLogService.saveLog(admin.getId(), job.getId(), "Duyệt bài tuyển dụng");
            ok = true;
        } else {
            job.setStatus(JobStatus.REJECTED);
            auditLogService.saveLog(admin.getId(), job.getId(), "Từ chối bài tuyển dụng");
        }
        job.setApprovedBy(admin);
        job.setApprovedAt(LocalDateTime.now());
        if (ok) {
            notificationService.createNotification(
                    job.getCompany().getUser().getEmail(),
                    "ApprovedJob",
                    "Admin đã duyệt bài viết của bạn",
                    "bài viết " + job.getTitle() + "đã được duyệt"
            );
        } else {
            notificationService.createNotification(
                    job.getCompany().getUser().getEmail(),
                    "RejectedJob",
                    "Admin đã duyệt bài viết của bạn",
                    "bài viết " + job.getTitle() + "đã bị từ chối"
            );
        }
        return jobMapper.toResponse(jobPostRepository.save(job));
    }

    @Override
    @Transactional
    public List<JobResponse> getActiveJobs() {
        List<JobPost> actionJobs = jobPostRepository.findActiveJobs();
        return jobMapper.toResponseList(actionJobs);
    }

    @Override
    @Transactional
    public JobResponse getJobDetail(Long id) {
        JobPost job = jobPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng"));
        Integer currentView = job.getViewCount() != null ? job.getViewCount() : 0;
        job.setViewCount(currentView + 1);
        JobPost savedJob = jobPostRepository.save(job);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isFav = false;

        // Kiểm tra xem user đã thực sự đăng nhập chưa (không phải ẩn danh)
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String email = auth.getName();
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isPresent()) {
                isFav = favoriteRepository.findByUserIdAndJobPostId(userOpt.get().getId(), id).isPresent();
            }
        }

        // 4. Map sang Response và gán giá trị isFavorite
        JobResponse response = jobMapper.toResponse(savedJob);
        response.setFavorite(isFav);

        return response;
    }

    private void handleJdFileUpload(JobRequest request, JobPost job) {
        System.out.println("JD FILE: " + request.getJdFile());
        if (request.getJdFile() != null && !request.getJdFile().isEmpty()) {
            try {
                String fileUrl = cloudinaryService.uploadFile(request.getJdFile(), "job_jds", "2");
                job.setJdFile(fileUrl);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi upload file JD lên Cloudinary: " + e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public List<JobResponse> searchByTitle(String title) {
        List<JobPost> jobs = jobPostRepository.findByTitleContainingIgnoreCase(title);
        return jobMapper.toResponseList(jobs);
    }

    @Override
    @Transactional
    public List<JobResponse> filterJobs(
            String location,
            String type,
            Double minSalary,
            Double maxSalary,
            List<String> tags
    ) {
        List<JobPost> jobs = jobPostRepository.filterJobs(
                location, type, minSalary, maxSalary
        );
        if (tags == null || tags.isEmpty()) {
            return jobMapper.toResponseList(jobs);
        }
        List<JobPost> filteredJobs = jobs.stream()
                .filter(job -> {
                    String jobTagsJson = job.getTags();
                    if (jobTagsJson == null) return false;
                    //anyMatch chỉ cần tìm 1 phần tử thỏa mãn điều kiện thì dừng lại ngay
                    String lowerTagsJson = jobTagsJson.toLowerCase();

                    return tags.stream().anyMatch(tag -> {
                        String lowerTag = tag.toLowerCase();
                        // Mẹo: Tìm "abc" (có ngoặc) trong chuỗi ["abc","def"]
                        return lowerTagsJson.contains("\"" + lowerTag + "\"");
                    });
                })
                .toList();

        return jobMapper.toResponseList(filteredJobs);
    }

    @Override
    @Transactional
    public List<JobResponse> getJobsByCompanyEmail(String email) {
        List<JobPost> jobs = jobPostRepository.findByCompanyUserEmailAndStatusNot(email, JobStatus.CLOSE);
//        List<JobPost> jobs = jobPostRepository.findByCompanyUserEmail(email);
        List<JobResponse> responseList = jobMapper.toResponseList(jobs);
        responseList.forEach(jobDto -> {
            Integer count = jobApplicationRepository.countByJobPostIdAndStatus(jobDto.getId(), ApplicationStatus.PENDING);
            jobDto.setQuantityApply(count);
        });
        return responseList;
    }
}
