package com.example.Backend.controller;

import com.example.Backend.entity.Technology;
import com.example.Backend.service.TechnologyService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/technologies")
public class TechnologyController {
    private final TechnologyService technologyService;

    // Lấy danh sách
    @GetMapping
    @Operation(summary = "Lấy danh sách công nghệ - public")
    public ResponseEntity<List<Technology>> getAllTechnologies() {
        return ResponseEntity.ok(technologyService.getAllTechnologies());
    }

    // Thêm mới
    @PostMapping
    @Operation(summary = "Thêm công nghê mới - chỉ admin - không cần viết trong dashboard -endponit này mình dự trù")
//    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> createTechnology(@RequestBody Technology technology) {
        try {
            Technology createdTech = technologyService.createTechnology(technology);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTech);
        } catch (IllegalArgumentException e) {
            // Lỗi 400 Bad Request nếu thiếu name
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            // Lỗi 409 Conflict nếu trùng tên
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
