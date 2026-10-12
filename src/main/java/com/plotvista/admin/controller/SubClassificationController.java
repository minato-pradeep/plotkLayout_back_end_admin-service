package com.plotvista.admin.controller;

import com.plotvista.admin.dto.ApiResponse;
import com.plotvista.admin.entity.SubClassification;
import com.plotvista.admin.service.SubClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sub-classifications")
@RequiredArgsConstructor
public class SubClassificationController {

    private final SubClassificationService subClassificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubClassification>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("Success", subClassificationService.getAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubClassification>> create(
            @RequestParam("name") String name,
            @RequestParam("classificationId") UUID classificationId,
            @RequestParam(value = "logo", required = false) MultipartFile logo) {
        try {
            SubClassification sc = subClassificationService.create(name, classificationId, logo);
            return ResponseEntity.ok(ApiResponse.ok("Sub-classification created", sc));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubClassification>> update(
            @PathVariable UUID id,
            @RequestParam("name") String name,
            @RequestParam(value = "classificationId", required = false) UUID classificationId,
            @RequestParam(value = "logo", required = false) MultipartFile logo) {
        try {
            SubClassification sc = subClassificationService.update(id, name, classificationId, logo);
            return ResponseEntity.ok(ApiResponse.ok("Sub-classification updated", sc));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        try {
            subClassificationService.delete(id);
            return ResponseEntity.ok(ApiResponse.ok("Sub-classification deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SubClassification>> toggleStatus(@PathVariable UUID id) {
        try {
            SubClassification sc = subClassificationService.toggleStatus(id);
            return ResponseEntity.ok(ApiResponse.ok("Status updated", sc));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
