package com.plotvista.admin.repository;

import com.plotvista.admin.entity.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, UUID> {
    List<ServiceProvider> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);
    List<ServiceProvider> findByCompanyIdAndActiveOrderByCreatedAtDesc(UUID companyId, boolean active);
}
