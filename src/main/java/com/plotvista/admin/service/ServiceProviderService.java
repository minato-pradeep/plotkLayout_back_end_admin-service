package com.plotvista.admin.service;

import com.plotvista.admin.entity.ServiceProvider;
import com.plotvista.admin.repository.ServiceProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceProviderService {

    private final ServiceProviderRepository repository;

    public List<ServiceProvider> getAll(UUID companyId) {
        return repository.findByCompanyIdOrderByCreatedAtDesc(companyId);
    }

    public List<ServiceProvider> getActive(UUID companyId) {
        return repository.findByCompanyIdAndActiveOrderByCreatedAtDesc(companyId, true);
    }

    public ServiceProvider getById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public ServiceProvider create(ServiceProvider provider) {
        return repository.save(provider);
    }

    @Transactional
    public ServiceProvider update(UUID id, ServiceProvider updates) {
        ServiceProvider existing = repository.findById(id).orElseThrow();
        
        if (updates.getName() != null) existing.setName(updates.getName());
        if (updates.getType() != null) existing.setType(updates.getType());
        if (updates.getWorkTypes() != null) existing.setWorkTypes(updates.getWorkTypes());
        if (updates.getPhones() != null) existing.setPhones(updates.getPhones());
        if (updates.getAddresses() != null) existing.setAddresses(updates.getAddresses());
        if (updates.getPhoto() != null) existing.setPhoto(updates.getPhoto());
        if (updates.getLogo() != null) existing.setLogo(updates.getLogo());
        if (updates.getLocationQuery() != null) existing.setLocationQuery(updates.getLocationQuery());
        if (updates.getRadius() > 0) existing.setRadius(updates.getRadius());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getExperience() > 0) existing.setExperience(updates.getExperience());
        if (updates.getWorkingHours() != null) existing.setWorkingHours(updates.getWorkingHours());
        
        return repository.save(existing);
    }

    @Transactional
    public void toggleActive(UUID id) {
        ServiceProvider provider = repository.findById(id).orElseThrow();
        provider.setActive(!provider.isActive());
        repository.save(provider);
    }

    @Transactional
    public void delete(UUID id) {
        repository.deleteById(id);
    }
}
