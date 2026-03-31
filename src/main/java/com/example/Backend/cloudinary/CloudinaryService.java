package com.example.Backend.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinaryAccount1;   // Account 1 - mặc định
    private final Cloudinary cloudinaryAccount2;   // Account 2

    /**
     * Upload avatar
     * account = "1" hoặc "2"
     */
    public String uploadAvatar(MultipartFile file, String folder, String account) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Cloudinary cloudinary = "2".equals(account) ? cloudinaryAccount2 : cloudinaryAccount1;

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image",
                "overwrite", true
        ));

        return (String) uploadResult.get("secure_url");
    }
    public String uploadFile(MultipartFile file, String folder, String account) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Cloudinary cloudinary = "2".equals(account) ? cloudinaryAccount2 : cloudinaryAccount1;

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "auto",        // auto để hỗ trợ PDF, image, raw...
                "overwrite", true
        ));

        return (String) uploadResult.get("secure_url");
    }
    /**
     * Xóa avatar với khả năng chọn account
     * account = "1" hoặc "2"
     */
    public void deleteAvatar(String publicId, String account) throws IOException {
        if (publicId == null || publicId.trim().isEmpty()) {
            return;
        }

        Cloudinary cloudinary = "2".equals(account) ? cloudinaryAccount2 : cloudinaryAccount1;

        cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                "resource_type", "image",
                "invalidate", true
        ));
    }

    /**
     * Trích xuất public_id từ URL Cloudinary
     */
    public String extractPublicId(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return null;
        }
        try {
            // Ví dụ URL: https://res.cloudinary.com/dyorinau8/image/upload/v177.../avatars/student/abc123.png
            String[] parts = imageUrl.split("/");
            for (int i = 0; i < parts.length; i++) {
                if ("upload".equals(parts[i]) && i + 2 < parts.length) {
                    String publicId = parts[i + 2];
                    // Bỏ phần extension (.png, .jpg...)
                    if (publicId.contains(".")) {
                        publicId = publicId.substring(0, publicId.lastIndexOf('.'));
                    }
                    return publicId;
                }
            }
            // Fallback
            return parts[parts.length - 1].split("\\.")[0];
        } catch (Exception e) {
            return null;
        }
    }
}