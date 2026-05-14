package com.example.Backend.service.Implement;

import com.example.Backend.dto.response.JobFavoriteResponse;
import com.example.Backend.entity.JobFavorite;
import com.example.Backend.entity.JobPost;
import com.example.Backend.entity.User;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.mapper.JobFavoriteMapper;
import com.example.Backend.repository.JobFavoriteRepository;
import com.example.Backend.repository.JobPostRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.JobFavoriteService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class JobFavoriteServiceImpl implements JobFavoriteService {
    private final JobFavoriteRepository favoriteRepository;
    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;
    private final JobFavoriteMapper favoriteMapper;

    @Override
    @Transactional
    public Object toggleFavorite(Long jobId, String email, String note) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        
        JobPost job = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Tin tuyển dụng không tồn tại"));

        Optional<JobFavorite> favoriteOpt = favoriteRepository.findByUserIdAndJobPostId(user.getId(), jobId);
        //kiểm tra có giá trị hay không
        if (favoriteOpt.isPresent()) {
            favoriteRepository.delete(favoriteOpt.get());
            return "Đã xóa khỏi danh sách yêu thích";
        } else {
            JobFavorite favorite = JobFavorite.builder()
                    .user(user)
                    .jobPost(job)
                    .note(note)
                    .createdAt(LocalDateTime.now())
                    .build();

            JobFavorite saved = favoriteRepository.save(favorite);
            return favoriteMapper.toResponse(saved);
        }
    }

    @Override
    public List<JobFavoriteResponse> getMyFavorites(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        List<JobFavorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return favoriteMapper.toResponseList(favorites);
    }
}
