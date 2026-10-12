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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final SubClassificationRepository subClassificationRepository;
    private final ProfessionRepository professionRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    @Value("${company.service.url}")
    private String companyServiceUrl;

    public List<Company> getAll() {
        return companyRepository.findAll();
    }

    public Company getById(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    public Company create(CompanyRequest req, MultipartFile logo) throws IOException {
        Company company = buildCompany(new Company(), req);

        if (logo != null && !logo.isEmpty()) {
            company.setLogo(fileStorageService.store(logo, "companies"));
        }

        company = companyRepository.save(company);

        // Create project in company-service immediately
        createProjectInCompanyService(company);

        // Trigger company-service setup and capture credentials
        Map<String, String> setupResult = triggerCompanySetup(company);
        if (setupResult != null) {
            String username = setupResult.get("username");
            String password = setupResult.get("password");
            // Set AFTER save so @Transient fields are not lost by JPA re-fetch
            company.setGeneratedUsername(username);
            company.setGeneratedPassword(password);
        }

        return company;
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

        // Notify company-service to delete company data
        try {
            sendDelete(companyServiceUrl + "/internal/setup/company/" + id);
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

    public Company retriggerSetup(UUID companyId) throws IOException {
        Company company = getById(companyId);
        Map<String, String> setupResult = triggerCompanySetup(company);
        if (setupResult != null) {
            company.setGeneratedUsername(setupResult.get("username"));
            company.setGeneratedPassword(setupResult.get("password"));
        }
        return company;
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
            HttpResponse<String> resp = sendPost(companyServiceUrl + "/internal/setup/company", json);
            if (resp != null) {
                return objectMapper.readValue(resp.body(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
            }

        } catch (Exception e) {
            log.error("Failed to trigger company setup in company-service: {}", e.getMessage());
        }
        return null;
    }

    private void createProjectInCompanyService(Company company) {
        try {
            Map<String, Object> projectPayload = new HashMap<>();
            projectPayload.put("companyName", company.getName());
            projectPayload.put("companyUsername", company.getGeneratedUsername() != null ? company.getGeneratedUsername() : "");
            projectPayload.put("companyLayoutType", company.getLayoutType());

            String json = objectMapper.writeValueAsString(projectPayload);
            HttpResponse<String> resp = sendPost(
                companyServiceUrl + "/api/projects/company/" + company.getId(),
                json
            );

            if (resp != null && resp.statusCode() == 200) {
                log.info("Project created in company-service for company: {}", company.getId());
            } else {
                log.warn("Failed to create project in company-service: {}", resp != null ? resp.statusCode() : "null");
            }
        } catch (Exception e) {
            log.error("Failed to create project in company-service: {}", e.getMessage());
        }
    }

    private HttpResponse<String> sendPost(String url, String json) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("company-service response [{}]: {}", response.statusCode(), response.body());
        return response;
    }

    private void sendDelete(String url) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE()
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
