package com.example.Backend.service;

import com.example.Backend.entity.Technology;

import java.util.List;

public interface TechnologyService {
    List<Technology> getAllTechnologies();
    Technology createTechnology(Technology technology);
}
