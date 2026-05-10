package com.example.Backend.entity;

import lombok.Builder;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "cv_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String fileName;        // Tên file gốc khi upload

    @Column(name = "file_path", nullable = false)
    private String filePath;        // Đường dẫn trên Cloudinary/S3

    @Column(name = "file_size")
    private Integer fileSize;       // Kích thước file (bytes)

    // Trong entity CvFile
    @OneToOne
    @JoinColumn(name = "cv_id", nullable = false)
    private Cv cv;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
}
