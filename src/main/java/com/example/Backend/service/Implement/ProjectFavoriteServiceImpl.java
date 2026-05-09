package com.example.Backend.service.Implement;

import com.example.Backend.dto.request.CreateProjectFavoriteRequest;
import com.example.Backend.dto.response.ProjectFavoriteResponse;
import com.example.Backend.entity.*;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.mapper.ProjectFavoriteMapper;
import com.example.Backend.repository.ProjectFavoriteRepository;
import com.example.Backend.repository.ProjectRepository;
import com.example.Backend.repository.StudentRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.ProjectFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectFavoriteServiceImpl implements ProjectFavoriteService {

    private final ProjectFavoriteRepository projectFavoriteRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectFavoriteMapper projectFavoriteMapper;
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public ProjectFavoriteResponse createFavorite(CreateProjectFavoriteRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Nguoi dung khong ton tai"));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project khong ton tai"));

        if (project.getStatus() != ProjectStatus.APPROVED) {
            throw new RuntimeException("Chi duoc luu project da duoc duyet");
        }

        if (projectFavoriteRepository.existsByUserIdAndProjectId(user.getId(), project.getId())) {
            throw new RuntimeException("Project da co trong danh sach yeu thich");
        }

        ProjectFavorite projectFavorite = ProjectFavorite.builder()
                .user(user)
                .project(project)
                .build();

        return projectFavoriteMapper.toResponse(projectFavoriteRepository.save(projectFavorite));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectFavoriteResponse> getMyFavorites(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Nguoi dung khong ton tai"));

        return projectFavoriteMapper.toResponseList(
                projectFavoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
        );
    }

    @Override
    @Transactional
    public void unlikeProject(Long projectId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Nguoi dung khong ton tai"));

        ProjectFavorite projectFavorite = projectFavoriteRepository.findByUserIdAndProjectId(user.getId(), projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project khong co trong danh sach yeu thich"));

        projectFavoriteRepository.delete(projectFavorite);
    }

    @Override
    @Transactional
    public boolean isProjectFavorited(Long projectId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Nguoi dung khong ton tai"));
        return projectFavoriteRepository.existsByUserIdAndProjectId(user.getId(), projectId);
    }
}
