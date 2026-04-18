package com.example.Backend.service;

import com.example.Backend.dto.request.CreateProjectFavoriteRequest;
import com.example.Backend.dto.response.ProjectFavoriteResponse;

import java.util.List;

public interface ProjectFavoriteService {

    ProjectFavoriteResponse createFavorite(CreateProjectFavoriteRequest request, String email);

    List<ProjectFavoriteResponse> getMyFavorites(String email);

    void unlikeProject(Long projectId, String email);
}
