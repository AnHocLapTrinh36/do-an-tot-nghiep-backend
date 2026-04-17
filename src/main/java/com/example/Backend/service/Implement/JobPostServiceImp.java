package com.example.Backend.service.Implement;

import com.example.Backend.cloudinary.CloudinaryService;
import com.example.Backend.dto.request.JobRequest;
import com.example.Backend.dto.response.JobResponse;
import com.example.Backend.entity.Company;
import com.example.Backend.entity.JobPost;
import com.example.Backend.entity.JobStatus;
import com.example.Backend.entity.User;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.mapper.JobMapper;
import com.example.Backend.repository.CompanyRepository;
import com.example.Backend.repository.JobFavoriteRepository;
import com.example.Backend.repository.JobPostRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.JobPostService;
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
        return jobMapper.toResponse(jobPostRepository.save(job));
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
        if (job.getStatus() == JobStatus.APPROVED) {
            job.setStatus(JobStatus.PENDING);
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
        return jobMapper.toResponse(jobPostRepository.save(job));
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
        JobPost job = jobPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin"));
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy admin thực hiện"));
        //kiem tra xem phai Approve khonh
        if ("APPROVE".equalsIgnoreCase(action)) {
            job.setStatus(JobStatus.APPROVED);
        } else {
            job.setStatus(JobStatus.REJECTED);
        }
        job.setApprovedBy(admin);
        job.setApprovedAt(LocalDateTime.now());
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
                // Upload lên thư mục "job_jds" trên Cloudinary với account "1"
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

        return jobMapper.toResponseList(jobs);
    }
}
