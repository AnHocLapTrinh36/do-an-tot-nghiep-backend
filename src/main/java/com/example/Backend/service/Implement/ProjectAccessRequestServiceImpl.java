package com.example.Backend.service.Implement;

import com.example.Backend.dto.request.CreateProjectAccessRequestRequest;
import com.example.Backend.dto.request.admin.AdminUpdateProjectAccessRequestStatusRequest;
import com.example.Backend.dto.response.ProjectAccessRequestResponse;
import com.example.Backend.entity.Company;
import com.example.Backend.entity.Project;
import com.example.Backend.entity.ProjectAccessRequest;
import com.example.Backend.entity.ProjectStatus;
import com.example.Backend.entity.Status;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.repository.CompanyRepository;
import com.example.Backend.repository.ProjectAccessRequestRepository;
import com.example.Backend.repository.ProjectRepository;
import com.example.Backend.service.ProjectAccessRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProjectAccessRequestServiceImpl implements ProjectAccessRequestService {

    private final ProjectAccessRequestRepository projectAccessRequestRepository;
    private final CompanyRepository companyRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public ProjectAccessRequestResponse createRequest(CreateProjectAccessRequestRequest request, String companyEmail) {
        Company company = companyRepository.findByUserEmail(companyEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay cong ty hien tai"));
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay project"));

        if (!Boolean.TRUE.equals(company.getIsVerified())) {
            throw new RuntimeException("Chi cong ty da duoc duyet moi co the gui yeu cau hop tac");
        }
        if (project.getStatus() != ProjectStatus.APPROVED) {
            throw new RuntimeException("Chi duoc gui yeu cau voi project da duoc duyet");
        }
        if (projectAccessRequestRepository.existsByCompany_IdAndProject_Id(company.getId(), project.getId())) {
            throw new RuntimeException("Ban da gui yeu cau cho project nay roi");
        }

        ProjectAccessRequest projectAccessRequest = new ProjectAccessRequest();
        projectAccessRequest.setCompany(company);
        projectAccessRequest.setProject(project);
        projectAccessRequest.setReason(request.getReason().trim());
        projectAccessRequest.setStatus(Status.PENDING);

        return toResponse(projectAccessRequestRepository.save(projectAccessRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectAccessRequestResponse> getMyRequests(String companyEmail, int page, int size) {
        Company company = companyRepository.findByUserEmail(companyEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay cong ty hien tai"));

        return projectAccessRequestRepository.findByCompany_Id(company.getId(), buildPageable(page, size))
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectAccessRequestResponse getMyRequestDetail(Long requestId, String companyEmail) {
        Company company = companyRepository.findByUserEmail(companyEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay cong ty hien tai"));

        ProjectAccessRequest projectAccessRequest = projectAccessRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay yeu cau"));

        if (!projectAccessRequest.getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Ban khong co quyen xem yeu cau nay");
        }

        return toResponse(projectAccessRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectAccessRequestResponse> getAllRequests(int page, int size) {
        return projectAccessRequestRepository.findAll(buildPageable(page, size))
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectAccessRequestResponse getRequestDetailForAdmin(Long requestId) {
        ProjectAccessRequest projectAccessRequest = projectAccessRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay yeu cau"));
        return toResponse(projectAccessRequest);
    }

    @Override
    @Transactional
    public ProjectAccessRequestResponse updateRequestStatus(Long requestId, AdminUpdateProjectAccessRequestStatusRequest request) {
        ProjectAccessRequest projectAccessRequest = projectAccessRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay yeu cau"));

        if (projectAccessRequest.getStatus() != Status.PENDING) {
            throw new RuntimeException("Chi yeu cau dang cho duyet moi co the cap nhat trang thai");
        }
        if (request.getStatus() == Status.PENDING) {
            throw new RuntimeException("Khong the cap nhat ve trang thai PENDING");
        }
        if (request.getStatus() == Status.REJECTED &&
                (request.getApprovalNote() == null || request.getApprovalNote().trim().isEmpty())) {
            throw new RuntimeException("Vui long nhap approval_note khi tu choi yeu cau");
        }

        projectAccessRequest.setStatus(request.getStatus());
        projectAccessRequest.setProcessedAt(LocalDateTime.now());
        projectAccessRequest.setApprovalNote(
                request.getApprovalNote() == null ? null : request.getApprovalNote().trim()
        );

        return toResponse(projectAccessRequestRepository.save(projectAccessRequest));
    }

    private PageRequest buildPageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(Sort.Direction.DESC, "requestedAt"));
    }

    private ProjectAccessRequestResponse toResponse(ProjectAccessRequest entity) {
        return ProjectAccessRequestResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompany().getId())
                .companyName(entity.getCompany().getCompanyName())
                .companyEmail(entity.getCompany().getUser() != null ? entity.getCompany().getUser().getEmail() : null)
                .companyPhone(entity.getCompany().getPhone())
                .companyWebsite(entity.getCompany().getWebsite())
                .projectId(entity.getProject().getId())
                .projectTitle(entity.getProject().getTitle())
                .studentName(
                        entity.getProject().getStudent() != null && entity.getProject().getStudent().getUser() != null
                                ? entity.getProject().getStudent().getUser().getFullName()
                                : null
                )
                .reason(entity.getReason())
                .status(entity.getStatus())
                .requestedAt(entity.getRequestedAt())
                .processedAt(entity.getProcessedAt())
                .approvalNote(entity.getApprovalNote())
                .build();
    }
}
