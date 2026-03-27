package com.example.Backend.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "teachers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_teacher_user")
    )
    private User user;
    @Column(name = "employee_code", unique = true)
    private String employeeCode;
}
