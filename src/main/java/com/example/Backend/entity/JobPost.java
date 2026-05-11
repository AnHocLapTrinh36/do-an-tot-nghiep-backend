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

    @Column(columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String requirements;
    @Column(columnDefinition = "NVARCHAR(50)")
    private String jobType;
    @Column(columnDefinition = "NVARCHAR(255)")
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

    @Column(columnDefinition = "NVARCHAR(255)")
    private String tags;
}
