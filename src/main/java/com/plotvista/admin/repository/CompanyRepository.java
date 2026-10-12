package com.plotvista.admin.repository;

import com.plotvista.admin.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
    boolean existsByEmail(String email);
}
