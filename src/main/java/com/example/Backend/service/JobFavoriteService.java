package com.example.Backend.service;

import com.example.Backend.dto.response.JobFavoriteResponse;

import java.util.List;

public interface JobFavoriteService {
    Object toggleFavorite(Long jobId, String email, String note);

    List<JobFavoriteResponse> getMyFavorites(String email);
}
