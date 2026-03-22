package com.example.Backend.dto.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserReponse {
    private Integer id;
    private String email;
    private String fullName;
    private String role;
    private Boolean isActive;
}