package com.example.Backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_favorites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private JobPost jobPost;

    @Column(columnDefinition = "TEXT")
    private String note;

    private LocalDateTime createdAt = LocalDateTime.now();
}
