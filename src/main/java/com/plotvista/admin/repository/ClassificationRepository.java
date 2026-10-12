package com.plotvista.admin.repository;

import com.plotvista.admin.entity.Classification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ClassificationRepository extends JpaRepository<Classification, UUID> {
    boolean existsByName(String name);
}
