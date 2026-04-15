package com.example.Backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;
    private String jobType;
    private String workLocation;
    private String jdFile;

    private LocalDateTime postedAt;

    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    private LocalDateTime approvedAt;
    private LocalDateTime startDay;
    private LocalDateTime endDay;

    private Integer viewCount = 0;
    private Integer appliedCount = 0;
    private Double salary;
    // lưu dạng json, lưu các ngôn ngữ, hoặc framework

    private String tags;
}
