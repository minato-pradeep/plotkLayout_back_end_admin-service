package com.plotvista.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plotvista.admin.dto.ApiResponse;
import com.plotvista.admin.dto.CompanyRequest;
import com.plotvista.admin.entity.Company;
import com.plotvista.admin.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Company>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("Success", companyService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Company>> getById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Success", companyService.getById(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(
            @RequestParam("data") String dataJson,
            @RequestParam(value = "logo", required = false) MultipartFile logo) {
        try {
            CompanyRequest req = objectMapper.readValue(dataJson, CompanyRequest.class);
            Company company = companyService.create(req, logo);
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("id", company.getId());
            result.put("name", company.getName());
            result.put("email", company.getEmail());
            result.put("layoutType", company.getLayoutType());
            result.put("generatedUsername", company.getGeneratedUsername());
            result.put("generatedPassword", company.getGeneratedPassword());
            return ResponseEntity.ok(ApiResponse.ok("Company created successfully", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Company>> update(
            @PathVariable UUID id,
            @RequestParam("data") String dataJson,
            @RequestParam(value = "logo", required = false) MultipartFile logo) {
        try {
            CompanyRequest req = objectMapper.readValue(dataJson, CompanyRequest.class);
            Company company = companyService.update(id, req, logo);
            return ResponseEntity.ok(ApiResponse.ok("Company updated successfully", company));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        try {
            companyService.delete(id);
            return ResponseEntity.ok(ApiResponse.ok("Company deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/retrigger-setup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> retriggerSetup(@PathVariable UUID id) {
        try {
            Company company = companyService.retriggerSetup(id);
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("id", company.getId());
            result.put("name", company.getName());
            result.put("generatedUsername", company.getGeneratedUsername());
            result.put("generatedPassword", company.getGeneratedPassword());
            return ResponseEntity.ok(ApiResponse.ok("Setup re-triggered successfully", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
