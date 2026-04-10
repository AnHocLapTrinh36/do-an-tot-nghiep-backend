package com.example.Backend.service;

import com.example.Backend.dto.request.CommentRequest;
import com.example.Backend.dto.response.CommentResponse;

import java.util.List;

public interface ProjectCommentService {
    /// danh sách cmt của 1 project
    List<CommentResponse> getCommentsByProject(Long projectId);
    /// bình luận vào project
    CommentResponse addComment(Long projectId, String userEmail, CommentRequest request);
}
