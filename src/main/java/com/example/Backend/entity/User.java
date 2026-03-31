package com.example.Backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

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
    @JsonIgnore
    private String password;  // NULL nếu chỉ GOOGLE

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;
    /// Map Student
    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Student student;
    /// Map Teacher
    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Teacher teacher;
    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Company company;
    //Eager: lấy ra khi vừa tạo xong
    //Lazy: khi cần mới lấy
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Admin admin;
    /// Map Company
    /// Map Admin
    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Admin admin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role;  //USER, ADMIN, TEACHER, ADMIN

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider = Provider.LOCAL; //xác định login bằng gì:LOCAL (email + password) GOOGLE

    @Column(name = "provider_id", length = 255)
    private String providerId;     // Google sub

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VerificationToken> tokens;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
