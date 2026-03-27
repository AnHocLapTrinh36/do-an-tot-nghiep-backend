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

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role;  //USER, ADMIN, TEACHER, ADMIN

    @Column( nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider = Provider.LOCAL; //xác định login bằng gì:LOCAL (email + password) GOOGLE

    @Column(name = "provider_id",length = 255)
    private String providerId;     // Google sub

    @Column(name = "is_active",nullable = false)
    private boolean isActive = true;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @CreationTimestamp
    @Column(name = "create_at",updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
