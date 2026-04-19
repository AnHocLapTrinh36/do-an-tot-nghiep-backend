package com.example.Backend.service.Implement;

import com.example.Backend.dto.request.ApplicationReviewRequest;
import com.example.Backend.dto.request.JobApplyRequest;
import com.example.Backend.dto.response.ApplicantResponse;
import com.example.Backend.dto.response.CompanyResponse;
import com.example.Backend.dto.response.JobApplicationResponse;
import com.example.Backend.entity.*;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.mapper.JobApplicationMapper;
import com.example.Backend.repository.*;
import com.example.Backend.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor

public class JobApplicationServiceImpl implements JobApplicationService {
    private final JobApplicationRepository applicationRepository;
    private final JobPostRepository jobPostRepository;
    private final StudentRepository studentRepository;
    private final CvRepository cvRepository;
    private final JobApplicationMapper applicationMapper;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public JobApplicationResponse applyJob(Long jobId, JobApplyRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));
        JobPost job = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Công việc không tồn tại"));
        if (!"APPROVED".equals(job.getStatus().name())) {
            throw new RuntimeException("Công việc này chưa được duyệt để ứng tuyển!");
        }
        if (job.getStartDay() != null && job.getStartDay().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Công việc này chưa đến thời gian bắt đầu nhận hồ sơ!");
        }
        if (job.getEndDay() != null && job.getEndDay().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Công việc này đã hết hạn ứng tuyển!");
        }
        Cv cv = cvRepository.findById(request.getCvId())
                .orElseThrow(() -> new RuntimeException("CV không tồn tại"));
        if (!cv.getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("Bạn không có quyền sử dụng CV này!");
        }
        if (applicationRepository.existsByStudentIdAndJobPostId(student.getId(), jobId)) {
            throw new RuntimeException(" Bạn đã ứng tuyển công việc này rồi!");
        }
        JobApplication application = JobApplication.builder()
                .student(student)
                .jobPost(job)
                .cv(cv)
                .status(ApplicationStatus.PENDING)
                .build();
        JobApplication savedApp = applicationRepository.save(application);
        int currentCount = job.getViewCount() == null ? 0 : job.getViewCount();
        job.setAppliedCount(currentCount + 1);
        jobPostRepository.save(job);

        return applicationMapper.toResponse(savedApp);
    }

    @Override
    @Transactional
    public List<ApplicantResponse> getApplicantsByJob(Long jobId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        JobPost job = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc với ID: " + jobId));

        if (!job.getCompany().getUser().getEmail().equals(email)) {
            throw new RuntimeException("Bạn không có quyền truy cập danh sách ứng viên của công việc này!");
        }

//        List<JobApplication> applications = applicationRepository.findByJobPostId(jobId);
        List<JobApplication> applications = applicationRepository.findByJobPostIdAndStatus(jobId, ApplicationStatus.PENDING);
        return applicationMapper.toApplicantResponseList(applications);
    }

    @Override
    @Transactional
    public List<ApplicantResponse> getJobApplicationsApproved() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Company company = companyRepository.findByUserEmail(email).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));
        List<JobApplication> applications = applicationRepository.findByJobPostCompanyIdAndStatus(company.getId(), ApplicationStatus.APPROVED);
        return applicationMapper.toApplicantResponseList(applications);
    }

    @Override
    @Transactional
    public JobApplicationResponse reviewApplication(Long applicationId, ApplicationReviewRequest request) {

        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Hồ sơ không tồn tại!"));
        app.setStatus(request.getStatus());
        app.setCompanyNote(request.getCompanyNote());
        applicationRepository.save(app);
        return applicationMapper.toResponse(app);
    }

    @Override
    @Transactional
    public List<JobApplication> test(Long jobId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        JobPost job = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc với ID: " + jobId));

        if (!job.getCompany().getUser().getEmail().equals(email)) {
            throw new RuntimeException("Bạn không có quyền truy cập danh sách ứng viên của công việc này!");
        }

        List<JobApplication> applications = applicationRepository.findByJobPostId(jobId);

        return applications;
    }
}