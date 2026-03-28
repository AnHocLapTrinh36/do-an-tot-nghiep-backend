package com.example.Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_student_user")
    )
    private User user;

    @Column(unique = true, length = 20,updatable = false)
    private String mssv;

    @Column(precision = 3, scale = 2)
    private BigDecimal gpa;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(length = 15)
    private String phone;

    @Column(length = 255)
    private String address;

    private Boolean gender;

    @Column(length = 20)
    private String course;

    @Column(length = 30)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at",updatable = false)
    private LocalDateTime createdAt;
}