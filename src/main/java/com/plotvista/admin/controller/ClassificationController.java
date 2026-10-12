package com.plotvista.admin.controller;

import com.plotvista.admin.dto.ApiResponse;
import com.plotvista.admin.entity.Classification;
import com.plotvista.admin.service.ClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/classifications")
@RequiredArgsConstructor
public class ClassificationController {

    private final ClassificationService classificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Classification>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("Success", classificationService.getAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Classification>> create(@RequestBody Map<String, String> body) {
        try {
            Classification c = classificationService.create(body.get("name"), body.get("description"));
            return ResponseEntity.ok(ApiResponse.ok("Classification created", c));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Classification>> update(@PathVariable UUID id,
                                                               @RequestBody Map<String, String> body) {
        try {
            Classification c = classificationService.update(id, body.get("name"), body.get("description"));
            return ResponseEntity.ok(ApiResponse.ok("Classification updated", c));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        try {
            classificationService.delete(id);
            return ResponseEntity.ok(ApiResponse.ok("Classification deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Classification>> updateStatus(@PathVariable UUID id,
                                                                      @RequestBody Map<String, String> body) {
        try {
            Classification c = classificationService.updateStatus(id, body.get("status"));
            return ResponseEntity.ok(ApiResponse.ok("Status updated", c));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
