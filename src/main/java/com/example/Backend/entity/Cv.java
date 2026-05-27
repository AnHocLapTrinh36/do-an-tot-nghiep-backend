package com.example.Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "cvs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cv {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CVType type = CVType.FORM;

    @Column(name = "content_json", columnDefinition = "NVARCHAR(MAX)")
    private String contentJson;

    // Trạng thái duyệt của CV
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "download_count")
    private Integer downloadCount = 0;

    @Column(name = "admin_note", columnDefinition = "NVARCHAR(MAX)")
    private String adminNote;

    @OneToOne(mappedBy = "cv", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CvFile cvFile;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
