package com.example.Backend.service;

import com.example.Backend.dto.request.CvRequest;
import com.example.Backend.dto.request.CvUploadRequest;
import com.example.Backend.dto.response.CvPreviewResponse;
import com.example.Backend.dto.response.CvResponse;
import com.example.Backend.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Service
public interface CvService {
    //method tạo cv từ form
    CvResponse createCvFromForm(CvRequest request, MultipartFile avatarFile);
    CvResponse uploadCv(CvUploadRequest request, MultipartFile file) throws IOException;    //method update cv từ form (dành cho admin duyệt cv)
    CvResponse updateCvFromForm(Long cvId, CvRequest request, MultipartFile avatarFile);

    ///danh sách cv của student hiện tại
    List<CvResponse> getMyCvs();
    /// danh sách tất cả cv
    List<CvResponse> getAllCvs();
    /// method lấy chi tiết 1 CV
    CvResponse getCvById(Long id);
    void deleteCv(Long id);
    // Admin duyệt hoặc từ chối CV
    CvResponse approveCv(Long cvId, Status status);

    CvPreviewResponse previewCv(Long id);

    Page<CvResponse> getPublicApprovedCvs(String keyword, String type, List<String> techs, int page, int size, String sortBy, String sortDir);
}
