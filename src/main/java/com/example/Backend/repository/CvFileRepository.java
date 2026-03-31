package com.example.Backend.repository;

import com.example.Backend.entity.Cv;
import com.example.Backend.entity.CvFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CvFileRepository extends JpaRepository<CvFile, Long> {
}
