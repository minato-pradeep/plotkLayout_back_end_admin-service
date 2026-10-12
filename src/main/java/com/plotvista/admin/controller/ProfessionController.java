package com.plotvista.admin.controller;

import com.plotvista.admin.dto.ApiResponse;
import com.plotvista.admin.entity.Profession;
import com.plotvista.admin.service.ProfessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/professions")
@RequiredArgsConstructor
public class ProfessionController {

    private final ProfessionService professionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Profession>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("Success", professionService.getAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Profession>> create(@RequestBody Map<String, String> body) {
        try {
            Profession p = professionService.create(body.get("name"));
            return ResponseEntity.ok(ApiResponse.ok("Profession created", p));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Profession>> update(@PathVariable UUID id,
                                                           @RequestBody Map<String, String> body) {
        try {
            Profession p = professionService.update(id, body.get("name"));
            return ResponseEntity.ok(ApiResponse.ok("Profession updated", p));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        try {
            professionService.delete(id);
            return ResponseEntity.ok(ApiResponse.ok("Profession deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Profession>> toggleStatus(@PathVariable UUID id) {
        try {
            Profession p = professionService.toggleStatus(id);
            return ResponseEntity.ok(ApiResponse.ok("Status updated", p));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
