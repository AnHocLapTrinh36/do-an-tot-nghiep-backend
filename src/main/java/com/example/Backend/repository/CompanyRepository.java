package com.example.Backend.repository;

import com.example.Backend.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;


public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByUserEmail(String email);
}
