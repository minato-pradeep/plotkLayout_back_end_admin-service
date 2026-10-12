package com.plotvista.admin.service;

import com.plotvista.admin.entity.Service;
import com.plotvista.admin.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public List<Service> getAll() {
        return serviceRepository.findAll();
    }

    public Service getById(UUID id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
    }

    public Service create(String name) {
        Service s = new Service();
        s.setName(name);
        return serviceRepository.save(s);
    }

    public Service update(UUID id, String name) {
        Service s = getById(id);
        s.setName(name);
        return serviceRepository.save(s);
    }

    public void delete(UUID id) {
        serviceRepository.deleteById(id);
    }

    public Service toggleStatus(UUID id) {
        Service s = getById(id);
        s.setStatus(s.getStatus() == Service.Status.ACTIVE
                ? Service.Status.INACTIVE
                : Service.Status.ACTIVE);
        return serviceRepository.save(s);
    }
}
