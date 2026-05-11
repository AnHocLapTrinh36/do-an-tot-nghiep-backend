package com.example.Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Getter
@Setter
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "company_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String companyName;

    @Column(name = "tax_code", unique = true)
    private String taxCode;

    private String website;
    @Column(columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;
    private String phone;
    @Column(columnDefinition = "NVARCHAR(255)")
    private String location;

    @Enumerated(EnumType.STRING)
    private CompanyStatus status = CompanyStatus.PENDING;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @ManyToOne
    @JoinColumn(name = "verified_by")
    private Admin verifiedBy;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
