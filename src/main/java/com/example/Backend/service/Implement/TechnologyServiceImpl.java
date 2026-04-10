package com.example.Backend.service.Implement;

import com.example.Backend.entity.Technology;
import com.example.Backend.repository.TechnologyRepository;
import com.example.Backend.service.TechnologyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TechnologyServiceImpl implements TechnologyService {
    private final TechnologyRepository technologyRepository;

    @Override
    public List<Technology> getAllTechnologies() {
        return technologyRepository.findAll();
    }

    @Override
    public Technology createTechnology(Technology technology) {
        // Validate bắt buộc phải có name
        if (technology.getName() == null || technology.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên công nghệ (name) không được để trống");
        }
        // Kiểm tra trùng lặp
        if (technologyRepository.existsByName(technology.getName().trim())) {
            throw new RuntimeException("Công nghệ này đã tồn tại: " + technology.getName());
        }
        technology.setName(technology.getName().trim());
        return technologyRepository.save(technology);
    }
}
