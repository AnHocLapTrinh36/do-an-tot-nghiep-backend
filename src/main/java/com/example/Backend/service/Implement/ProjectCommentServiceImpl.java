package com.example.Backend.service.Implement;

import com.example.Backend.dto.request.CommentRequest;
import com.example.Backend.dto.response.CommentResponse;
import com.example.Backend.entity.Project;
import com.example.Backend.entity.ProjectComment;
import com.example.Backend.entity.User;
import com.example.Backend.repository.ProjectCommentRepository;
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
    // Tương lai: private final ProjectPurchaseRepository projectPurchaseRepository;
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

        // GỌI HÀM VALIDATE ĐỂ KIỂM TRA QUYỀN COMMENT
        validateCommentPermission(project, user);

        //NẾU HỢP LỆ THÌ LƯU COMMENT
        ProjectComment comment = ProjectComment.builder()
                .project(project)
                .user(user)
                .content(request.getContent())
                .build();

        ProjectComment savedComment = projectCommentRepository.save(comment);
        return mapToResponse(savedComment);
    }

    /**
     * HÀM MỞ RỘNG: Xử lý logic ai được phép comment
     */
    private void validateCommentPermission(Project project, User user) {
        // VẾ 1: Project FREE -> Ai cũng được comment
        if (project.getPriceType() != null && project.getPriceType().name().equals("FREE")) {
            return; // Hợp lệ, pass qua
        }

        // VẾ 2: Project PAID -> Chỉ người đã mua mới được comment
        if (project.getPriceType() != null && project.getPriceType().name().equals("PAID")) {
            /* * TODO (PHASE 2 - THANH TOÁN): Mở comment đoạn code dưới đây khi làm xong module thanh toán
             * * boolean hasPurchased = projectPurchaseRepository.existsByProjectIdAndUserId(project.getId(), user.getId());
             * if (!hasPurchased) {
             * throw new RuntimeException("Bạn cần mua đồ án này để có thể bình luận.");
             * }
             */

            // Tạm thời return để không chặn luồng test ở flow 1 (project free)
            return;
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
