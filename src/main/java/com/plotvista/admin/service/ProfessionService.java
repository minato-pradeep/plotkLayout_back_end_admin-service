package com.plotvista.admin.service;

import com.plotvista.admin.entity.Profession;
import com.plotvista.admin.repository.ProfessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfessionService {

    private final ProfessionRepository professionRepository;

    public List<Profession> getAll() {
        return professionRepository.findAll();
    }

    public Profession getById(UUID id) {
        return professionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profession not found"));
    }

    public Profession create(String name) {
        Profession p = new Profession();
        p.setName(name);
        return professionRepository.save(p);
    }

    public Profession update(UUID id, String name) {
        Profession p = getById(id);
        p.setName(name);
        return professionRepository.save(p);
    }

    public void delete(UUID id) {
        professionRepository.deleteById(id);
    }

    public Profession toggleStatus(UUID id) {
        Profession p = getById(id);
        p.setStatus(p.getStatus() == Profession.Status.ACTIVE
                ? Profession.Status.INACTIVE
                : Profession.Status.ACTIVE);
        return professionRepository.save(p);
    }
}
