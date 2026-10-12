package com.plotvista.admin.controller;

import com.plotvista.admin.dto.ApiResponse;
import com.plotvista.admin.entity.Service;
import com.plotvista.admin.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Service>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("Success", serviceService.getAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Service>> create(@RequestBody Map<String, String> body) {
        try {
            Service s = serviceService.create(body.get("name"));
            return ResponseEntity.ok(ApiResponse.ok("Service created", s));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Service>> update(@PathVariable UUID id,
                                                        @RequestBody Map<String, String> body) {
        try {
            Service s = serviceService.update(id, body.get("name"));
            return ResponseEntity.ok(ApiResponse.ok("Service updated", s));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        try {
            serviceService.delete(id);
            return ResponseEntity.ok(ApiResponse.ok("Service deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Service>> toggleStatus(@PathVariable UUID id) {
        try {
            Service s = serviceService.toggleStatus(id);
            return ResponseEntity.ok(ApiResponse.ok("Status updated", s));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
