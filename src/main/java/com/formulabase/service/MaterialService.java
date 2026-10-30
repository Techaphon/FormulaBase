package com.formulabase.service;

import com.formulabase.domain.Material;
import com.formulabase.domain.MaterialUnit;
import com.formulabase.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MaterialService {
    private final MaterialRepository repository;

    public MaterialService(MaterialRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Material> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return repository.findAll();
        return repository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseOrderByCode(keyword, keyword);
    }

    @Transactional
    public Material create(String code, String name, MaterialUnit unit) {
        if (code == null || code.isBlank() || name == null || name.isBlank() || unit == null) {
            throw new IllegalArgumentException("Code, name and unit are required");
        }
        if (repository.existsByCodeIgnoreCase(code.trim())) {
            throw new IllegalArgumentException("Material code already exists");
        }
        return repository.save(new Material(code.trim(), name.trim(), unit));
    }
}
