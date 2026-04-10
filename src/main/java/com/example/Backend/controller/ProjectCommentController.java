package com.example.Backend.controller;


import com.example.Backend.dto.request.CommentRequest;
import com.example.Backend.dto.response.CommentResponse;
import com.example.Backend.service.ProjectCommentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment-projects")
@RequiredArgsConstructor
public class ProjectCommentController {
    private final ProjectCommentService projectCommentService;
    // Lấy danh sách comment của 1 project
    @GetMapping("/{projectId}/comments")
    @Operation(summary = "Lấy danh sách comment của 1 project")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectCommentService.getCommentsByProject(projectId));
    }

    // Thêm comment vào 1 project
    @PostMapping("/{projectId}/comments")
    @Operation(summary = "Thêm comment vào 1 project (free thì ai cũng có  thể  cmt, paid thì chỉ những người mua (chưa làm vì đang đợi flow thanh toán)")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long projectId,
            @RequestBody CommentRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userEmail = authentication.getName();
        return ResponseEntity.ok(projectCommentService.addComment(projectId, userEmail, request));
    }
}
