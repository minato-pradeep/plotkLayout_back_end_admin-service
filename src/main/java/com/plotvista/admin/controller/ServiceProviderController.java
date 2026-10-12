package com.plotvista.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plotvista.admin.entity.ServiceProvider;
import com.plotvista.admin.service.ServiceProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/service-providers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ServiceProviderController {

    private final ServiceProviderService service;

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam UUID companyId) {
        List<ServiceProvider> providers = service.getAll(companyId);
        return ResponseEntity.ok(Map.of("success", true, "data", providers));
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActive(@RequestParam UUID companyId) {
        List<ServiceProvider> providers = service.getActive(companyId);
        return ResponseEntity.ok(Map.of("success", true, "data", providers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        ServiceProvider provider = service.getById(id);
        if (provider == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("success", true, "data", provider));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> create(
            @RequestParam("companyId") UUID companyId,
            @RequestParam("data") String dataJson,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "logo", required = false) MultipartFile logo) {
        try {
            // Parse JSON data
            ObjectMapper mapper = new ObjectMapper();
            ServiceProvider provider = mapper.readValue(dataJson, ServiceProvider.class);
            provider.setCompanyId(companyId);
            
            // File uploads handled by frontend for now (base64 or URLs)
            
            ServiceProvider created = service.create(provider);
            return ResponseEntity.ok(Map.of("success", true, "data", created));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> update(
            @PathVariable UUID id,
            @RequestParam("companyId") UUID companyId,
            @RequestParam("data") String dataJson,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "logo", required = false) MultipartFile logo) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ServiceProvider updates = mapper.readValue(dataJson, ServiceProvider.class);
            updates.setCompanyId(companyId);
            
            // File uploads handled by frontend for now (base64 or URLs)
            
            ServiceProvider updated = service.update(id, updates);
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleActive(@PathVariable UUID id) {
        service.toggleActive(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Status toggled"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Deleted"));
    }
}
