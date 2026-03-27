package com.example.Backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;


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
    @JsonIgnore
    private User user;
    @Column(name = "employee_code", unique = true)
    private String employeeCode;
    @ManyToMany(mappedBy = "teachers")
    @ToString.Exclude
    @JsonIgnore
    private Set<Course> courses = new HashSet<>();
}
