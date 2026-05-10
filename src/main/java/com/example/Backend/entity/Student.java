package com.example.Backend.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    @JsonManagedReference
    private User user;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Cv> cvs;

    @Column(unique = true, length = 20, updatable = false)
    private String mssv;

    @Column(precision = 3, scale = 2)
    private BigDecimal gpa;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(length = 15)
    private String phone;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String address;

    private Boolean gender;

    @Column(columnDefinition = "NVARCHAR(20)")
    private String course;

    @Column(columnDefinition = "NVARCHAR(30)")
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
