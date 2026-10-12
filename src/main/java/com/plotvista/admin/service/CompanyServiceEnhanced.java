package com.plotvista.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plotvista.admin.dto.CompanyRequest;
import com.plotvista.admin.entity.Company;
import com.plotvista.admin.entity.CompanyContact;
import com.plotvista.admin.entity.SubClassification;
import com.plotvista.admin.entity.Profession;
import com.plotvista.admin.repository.CompanyRepository;
import com.plotvista.admin.repository.SubClassificationRepository;
import com.plotvista.admin.repository.ProfessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
public class CompanyServiceEnhanced {

    private final CompanyRepository companyRepository;
    private final SubClassificationRepository subClassificationRepository;
    private final ProfessionRepository professionRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    @Value("${company.service.url}")
    private String companyServiceUrl;

    @Value("${company.creation.max.retries:3}")
    private int maxRetries;

    @Value("${company.creation.timeout.ms:30000}")
    private long timeoutMs;

    public List<Company> getAll() {
        return companyRepository.findAll();
    }

    public Company getById(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    public Company create(CompanyRequest req, MultipartFile logo) throws IOException {
        long startTime = System.currentTimeMillis();
        UUID companyId = null;

        try {
            // Validate request
            validateCompanyRequest(req);
            log.info("📋 Company creation initiated: {}", req.getName());

            // Build and save company
            Company company = buildCompany(new Company(), req);

            if (logo != null && !logo.isEmpty()) {
                company.setLogo(fileStorageService.store(logo, "companies"));
            }

            company = companyRepository.save(company);
            companyId = company.getId();
            log.info("✅ Company saved to admin-service: {}", companyId);

            // Create project with retry logic
            boolean projectCreated = createProjectWithRetry(company, maxRetries);

            if (!projectCreated) {
                String errorMsg = "Failed to create project in company-service after " + maxRetries + " attempts";
                log.error("❌ {}", errorMsg);
                throw new RuntimeException(errorMsg);
            }

            log.info("✅ Project created in company-service: {}", companyId);

            // Verify project creation
            if (!verifyProjectCreation(companyId)) {
                String errorMsg = "Project verification failed for company: " + companyId;
                log.error("❌ {}", errorMsg);
                throw new RuntimeException(errorMsg);
            }

            log.info("✅ Project verified: {}", companyId);

            // Trigger company setup
            Map<String, String> setupResult = triggerCompanySetup(company);
            if (setupResult != null) {
                company.setGeneratedUsername(setupResult.get("username"));
                company.setGeneratedPassword(setupResult.get("password"));
                company = companyRepository.save(company);
                log.info("✅ Company setup completed: {}", companyId);
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ Company created successfully: {} ({}ms)", companyId, duration);

            return company;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Company creation failed: {} ({}ms) - {}", companyId, duration, e.getMessage(), e);
            throw e;
        }
    }

    private void validateCompanyRequest(CompanyRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Company name is required");
        }
        if (req.getLayoutType() == null || req.getLayoutType().isBlank()) {
            throw new IllegalArgumentException("Layout type is required");
        }
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        log.info("✅ Company request validated");
    }

    private boolean createProjectWithRetry(Company company, int maxRetries) {
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                Map<String, Object> projectPayload = new HashMap<>();
                projectPayload.put("companyName", company.getName());
                projectPayload.put("companyUsername", company.getGeneratedUsername() != null ? company.getGeneratedUsername() : "");
                projectPayload.put("companyLayoutType", company.getLayoutType());

                String json = objectMapper.writeValueAsString(projectPayload);
                HttpResponse<String> resp = sendPost(
                    companyServiceUrl + "/api/projects/company/" + company.getId(),
                    json,
                    timeoutMs
                );

                if (resp != null && resp.statusCode() == 200) {
                    log.info("✅ Project created successfully (attempt {}/{})", attempt + 1, maxRetries);
                    return true;
                }

                attempt++;
                if (attempt < maxRetries) {
                    long backoffMs = 1000L * attempt;
                    log.warn("⚠️ Project creation failed (attempt {}/{}), retrying in {}ms...", attempt, maxRetries, backoffMs);
                    Thread.sleep(backoffMs);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("❌ Project creation interrupted: {}", e.getMessage());
                return false;
            } catch (Exception e) {
                attempt++;
                if (attempt < maxRetries) {
                    long backoffMs = 1000L * attempt;
                    log.warn("⚠️ Project creation attempt {}/{} failed: {}, retrying in {}ms...", attempt, maxRetries, e.getMessage(), backoffMs);
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                } else {
                    log.error("❌ Project creation failed after {} attempts: {}", maxRetries, e.getMessage());
                }
            }
        }

        return false;
    }

    private boolean verifyProjectCreation(UUID companyId) {
        try {
            HttpResponse<String> resp = sendGet(
                companyServiceUrl + "/api/projects/company/" + companyId + "/validate",
                timeoutMs
            );

            if (resp != null && resp.statusCode() == 200) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) objectMapper.readValue(resp.body(), Map.class);
                Object data = responseBody.get("data");

                if (data instanceof Boolean && (Boolean) data) {
                    log.info("✅ Project verification successful");
                    return true;
                }
            }

            log.error("❌ Project verification failed: status={}", resp != null ? resp.statusCode() : "null");
            return false;

        } catch (Exception e) {
            log.error("❌ Project verification error: {}", e.getMessage());
            return false;
        }
    }

    public Company update(UUID id, CompanyRequest req, MultipartFile logo) throws IOException {
        Company company = getById(id);
        buildCompany(company, req);

        if (logo != null && !logo.isEmpty()) {
            fileStorageService.delete(company.getLogo());
            company.setLogo(fileStorageService.store(logo, "companies"));
        }

        return companyRepository.save(company);
    }

    public void delete(UUID id) {
        Company company = getById(id);
        fileStorageService.delete(company.getLogo());

        try {
            sendDelete(companyServiceUrl + "/internal/setup/company/" + id, timeoutMs);
        } catch (Exception e) {
            log.warn("Could not notify company-service on delete: {}", e.getMessage());
        }

        companyRepository.deleteById(id);
    }

    private Company buildCompany(Company company, CompanyRequest req) {
        company.setName(req.getName());
        company.setEmail(req.getEmail());
        company.setAddress(req.getAddress());
        company.setLatitude(req.getLatitude());
        company.setLongitude(req.getLongitude());
        company.setSquareFeet(req.getSquareFeet());
        company.setLayoutType(req.getLayoutType());
        company.setContractStart(req.getContractStart());
        company.setContractEnd(req.getContractEnd());

        if (req.getStatus() != null) {
            company.setStatus(Company.Status.valueOf(req.getStatus()));
        }

        company.getContacts().clear();
        if (req.getContacts() != null) {
            for (CompanyRequest.ContactDto cd : req.getContacts()) {
                CompanyContact contact = new CompanyContact();
                contact.setCompany(company);
                contact.setContactName(cd.getContactName());
                contact.setContactMobile(cd.getContactMobile());
                contact.setPrimary(cd.isPrimary());
                company.getContacts().add(contact);
            }
        }

        return company;
    }

    public void retriggerSetup(UUID companyId) throws IOException {
        Company company = getById(companyId);
        triggerCompanySetup(company);
    }

    private Map<String, String> triggerCompanySetup(Company company) {
        try {
            List<SubClassification> subClasses = subClassificationRepository.findAll();
            List<Profession> professions = professionRepository.findAll();

            Map<String, Object> payload = new HashMap<>();
            payload.put("companyId", company.getId().toString());
            payload.put("companyName", company.getName());
            payload.put("companyLayoutType", company.getLayoutType());
            payload.put("companyEmail", company.getEmail());

            List<Map<String, Object>> scList = new ArrayList<>();
            for (SubClassification sc : subClasses) {
                Map<String, Object> scMap = new HashMap<>();
                scMap.put("name", sc.getName());
                scMap.put("logo", sc.getLogo());
                scMap.put("classificationName", sc.getClassification().getName());
                scMap.put("status", sc.getStatus().name());
                scList.add(scMap);
            }
            payload.put("subClassifications", scList);

            List<Map<String, Object>> profList = new ArrayList<>();
            for (Profession p : professions) {
                Map<String, Object> profMap = new HashMap<>();
                profMap.put("name", p.getName());
                profMap.put("status", p.getStatus().name());
                profList.add(profMap);
            }
            payload.put("professions", profList);

            String json = objectMapper.writeValueAsString(payload);
            HttpResponse<String> resp = sendPost(companyServiceUrl + "/internal/setup/company", json, timeoutMs);

            if (resp != null && resp.statusCode() == 200) {
                Map<String, String> result = objectMapper.readValue(resp.body(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
                return result;
            }

        } catch (Exception e) {
            log.error("Failed to trigger company setup: {}", e.getMessage());
        }
        return null;
    }

    private HttpResponse<String> sendPost(String url, String json, long timeoutMs) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofMillis(timeoutMs))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendGet(String url, long timeoutMs) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(java.time.Duration.ofMillis(timeoutMs))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void sendDelete(String url, long timeoutMs) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(java.time.Duration.ofMillis(timeoutMs))
                .DELETE()
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
