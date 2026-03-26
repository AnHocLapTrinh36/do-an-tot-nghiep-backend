package com.example.Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", length = 255)
    private String password;  // NULL nếu chỉ GOOGLE

    @Column(nullable = false, length = 255)
    private String fullName;

    @Column(name= "avatar_url",length = 512)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role;  //USER, ADMIN, TEACHER, ADMIN

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider = Provider.LOCAL; //xác định login bằng gì:LOCAL (email + password) GOOGLE

    @Column(length = 255)
    private String providerId;     // Google sub

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
