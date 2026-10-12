package com.plotvista.admin.service;

import com.plotvista.admin.entity.SubClassification;
import com.plotvista.admin.repository.ClassificationRepository;
import com.plotvista.admin.repository.SubClassificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubClassificationService {

    private final SubClassificationRepository subClassificationRepository;
    private final ClassificationRepository classificationRepository;
    private final FileStorageService fileStorageService;

    public List<SubClassification> getAll() {
        return subClassificationRepository.findAllByOrderByClassificationNameAscNameAsc();
    }

    public SubClassification getById(UUID id) {
        return subClassificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sub-classification not found"));
    }

    public SubClassification create(String name, UUID classificationId, MultipartFile logo) throws IOException {
        var classification = classificationRepository.findById(classificationId)
                .orElseThrow(() -> new RuntimeException("Classification not found"));

        SubClassification sc = new SubClassification();
        sc.setName(name);
        sc.setClassification(classification);

        if (logo != null && !logo.isEmpty()) {
            sc.setLogo(fileStorageService.store(logo, "sub-classifications"));
        }

        return subClassificationRepository.save(sc);
    }

    public SubClassification update(UUID id, String name, UUID classificationId, MultipartFile logo) throws IOException {
        SubClassification sc = getById(id);
        sc.setName(name);

        if (classificationId != null) {
            var classification = classificationRepository.findById(classificationId)
                    .orElseThrow(() -> new RuntimeException("Classification not found"));
            sc.setClassification(classification);
        }

        if (logo != null && !logo.isEmpty()) {
            fileStorageService.delete(sc.getLogo());
            sc.setLogo(fileStorageService.store(logo, "sub-classifications"));
        }

        return subClassificationRepository.save(sc);
    }

    public void delete(UUID id) {
        SubClassification sc = getById(id);
        fileStorageService.delete(sc.getLogo());
        subClassificationRepository.deleteById(id);
    }

    public SubClassification toggleStatus(UUID id) {
        SubClassification sc = getById(id);
        sc.setStatus(sc.getStatus() == SubClassification.Status.ACTIVE
                ? SubClassification.Status.INACTIVE
                : SubClassification.Status.ACTIVE);
        return subClassificationRepository.save(sc);
    }
}
