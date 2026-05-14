package com.example.Backend.service.Implement;

import com.example.Backend.dto.request.CommentRequest;
import com.example.Backend.dto.response.CommentResponse;
import com.example.Backend.entity.PriceType;
import com.example.Backend.entity.Project;
import com.example.Backend.entity.ProjectComment;
import com.example.Backend.entity.User;
import com.example.Backend.repository.ProjectCommentRepository;
import com.example.Backend.repository.ProjectPurchaseRepository;
import com.example.Backend.repository.ProjectRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.ProjectCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectCommentServiceImpl implements ProjectCommentService {
    private final ProjectCommentRepository projectCommentRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectPurchaseRepository projectPurchaseRepository;

    @Override
    public List<CommentResponse> getCommentsByProject(Long projectId) {
        return projectCommentRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public CommentResponse addComment(Long projectId, String userEmail, CommentRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đồ án"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        validateCommentPermission(project, user);

        ProjectComment comment = ProjectComment.builder()
                .project(project)
                .user(user)
                .content(request.getContent())
                .build();

        ProjectComment savedComment = projectCommentRepository.save(comment);
        return mapToResponse(savedComment);
    }

    private void validateCommentPermission(Project project, User user) {
        if (project.getPriceType() != PriceType.PAID) {
            return;
        }

        boolean hasPurchased = projectPurchaseRepository.existsByProjectIdAndUserId(project.getId(), user.getId());
        if (!hasPurchased) {
            throw new RuntimeException("Bạn cần mua source code của đồ án này để có thể bình luận");
        }
    }

    private CommentResponse mapToResponse(ProjectComment comment) {
        CommentResponse dto = new CommentResponse();
        dto.setId(comment.getId());
        dto.setUserId(comment.getUser().getId());
        dto.setFullName(comment.getUser().getFullName());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}
