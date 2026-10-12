package com.plotvista.admin.repository;

import com.plotvista.admin.entity.SubClassification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SubClassificationRepository extends JpaRepository<SubClassification, UUID> {
    List<SubClassification> findByClassificationId(UUID classificationId);
    List<SubClassification> findAllByOrderByClassificationNameAscNameAsc();
    boolean existsByNameIgnoreCaseAndClassificationId(String name, UUID classificationId);
}
