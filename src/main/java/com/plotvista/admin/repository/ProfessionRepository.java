package com.plotvista.admin.repository;

import com.plotvista.admin.entity.Profession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProfessionRepository extends JpaRepository<Profession, UUID> {
    boolean existsByName(String name);
}
