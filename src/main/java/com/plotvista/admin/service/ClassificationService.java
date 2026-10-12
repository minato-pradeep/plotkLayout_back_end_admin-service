package com.plotvista.admin.service;

import com.plotvista.admin.entity.Classification;
import com.plotvista.admin.repository.ClassificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassificationService {

    private final ClassificationRepository classificationRepository;

    public List<Classification> getAll() {
        return classificationRepository.findAll();
    }

    public Classification getById(UUID id) {
        return classificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classification not found"));
    }

    public Classification create(String name, String description) {
        Classification c = new Classification();
        c.setName(name);
        c.setDescription(description);
        return classificationRepository.save(c);
    }

    public Classification update(UUID id, String name, String description) {
        Classification c = getById(id);
        c.setName(name);
        c.setDescription(description);
        return classificationRepository.save(c);
    }

    public void delete(UUID id) {
        classificationRepository.deleteById(id);
    }

    public Classification updateStatus(UUID id, String status) {
        Classification c = getById(id);
        c.setStatus(Classification.Status.valueOf(status.toUpperCase()));
        return classificationRepository.save(c);
    }
}
