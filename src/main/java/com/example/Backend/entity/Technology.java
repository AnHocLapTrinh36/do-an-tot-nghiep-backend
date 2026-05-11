package com.example.Backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "technologies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Technology {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String name;

    @Column(name = "icon_url")
    private String iconUrl;
}
