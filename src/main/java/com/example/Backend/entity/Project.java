package com.example.Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Giả sử bạn đã có Student và Course Entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "source_code_url")
    private String sourceCodeUrl;

    @Column(name = "demo_url")
    private String demoUrl;

    @Column(name = "view_count", columnDefinition = "int default 0")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "download_count", columnDefinition = "int default 0")
    @Builder.Default
    private Integer downloadCount = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type")
    private PriceType priceType;

    @Column(name = "price_download")
    private BigDecimal priceDownload;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;


    // QUAN HỆ NHIỀU - NHIỀU (BẢNG TRUNG GIAN project_technologies)
    @ManyToMany
    @JoinTable(
            name = "project_technologies",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "technology_id")
    )
    @Builder.Default
    private Set<Technology> technologies = new HashSet<>();

    // QUAN HỆ 1 - NHIỀU (BẢNG project_images)
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProjectImage> images = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method để add image đồng bộ 2 chiều
    public void addImage(ProjectImage image) {
        images.add(image);
        image.setProject(this);
    }
}
