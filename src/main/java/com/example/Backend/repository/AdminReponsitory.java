package com.example.Backend.repository;

import com.example.Backend.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminReponsitory extends JpaRepository<Admin, Long> {
    
}
