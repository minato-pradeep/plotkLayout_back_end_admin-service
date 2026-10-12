package com.plotvista.admin.config;

import com.plotvista.admin.entity.*;
import com.plotvista.admin.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final ClassificationRepository classificationRepository;
    private final SubClassificationRepository subClassificationRepository;
    private final ProfessionRepository professionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedSuperAdmin();
        seedClassifications();
        seedSubClassifications();
        seedProfessions();
    }

    private void seedSuperAdmin() {
        if (!adminUserRepository.existsByUsername("superadmin")) {
            AdminUser admin = new AdminUser();
            admin.setUsername("superadmin");
            admin.setPassword(passwordEncoder.encode("SuperAdmin@123"));
            admin.setEmail("admin@plotvista.com");
            admin.setRole(AdminUser.Role.SUPER_ADMIN);
            admin.setStatus(AdminUser.Status.ACTIVE);
            adminUserRepository.save(admin);
            log.info("Super admin created: superadmin / SuperAdmin@123");
        }
    }

    private void seedClassifications() {
        if (classificationRepository.count() == 0) {
            String[][] data = {
                {"Residential", "Residential plots including houses and apartments"},
                {"Commercial", "Commercial plots including shops and offices"},
                {"Others", "Other plot types including parks, roads and utilities"}
            };
            for (String[] d : data) {
                Classification c = new Classification();
                c.setName(d[0]);
                c.setDescription(d[1]);
                c.setStatus(Classification.Status.ACTIVE);
                classificationRepository.save(c);
            }
            log.info("Seeded 3 classifications");
        }
    }

    private void seedSubClassifications() {
        var residential = classificationRepository.findAll().stream()
                .filter(c -> c.getName().equals("Residential")).findFirst().orElse(null);
        var commercial = classificationRepository.findAll().stream()
                .filter(c -> c.getName().equals("Commercial")).findFirst().orElse(null);
        var others = classificationRepository.findAll().stream()
                .filter(c -> c.getName().equals("Others")).findFirst().orElse(null);

        if (residential == null || commercial == null || others == null) return;

        Object[][] data = {
            {"House", residential, "assets/plot-icons/house.png"},
            {"Apartment", residential, "assets/plot-icons/apartment.png"},
            {"PG", residential, "assets/plot-icons/apartment.png"},
            {"Guest House", residential, "assets/plot-icons/house.png"},
            {"Shop", commercial, "assets/plot-icons/shop.png"},
            {"Grocery Store", commercial, "assets/plot-icons/grosary.png"},
            {"Gym", commercial, "assets/plot-icons/gym.png"},
            {"Pharmacy", commercial, "assets/plot-icons/pharmacy.png"},
            {"Bakery", commercial, "assets/plot-icons/bakery.png"},
            {"Bank", commercial, "assets/plot-icons/bank.png"},
            {"Mini Clinic", commercial, "assets/plot-icons/mini clinic.png"},
            {"Laundry", commercial, "assets/plot-icons/laundary (1).png"},
            {"Tailor", commercial, "assets/plot-icons/tailor (1).png"},
            {"Spa", commercial, "assets/plot-icons/spa (1).png"},
            {"PlayStation", commercial, "assets/plot-icons/playstation.png"},
            {"Park", others, "assets/plot-icons/park.png"},
            {"Water Tank", others, "assets/plot-icons/watertank.png"},
            {"Hospital", others, "assets/plot-icons/hospital.png"},
            {"Security", others, "assets/plot-icons/security.png"},
            {"Road H", others, ""},
            {"Road V", others, ""},
            {"Gate", others, ""},
            {"Vacant", others, "assets/plot-icons/vacant.png"}
        };

        for (Object[] d : data) {
            String name = (String) d[0];
            Classification cls = (Classification) d[1];
            if (!subClassificationRepository.existsByNameIgnoreCaseAndClassificationId(name, cls.getId())) {
                SubClassification sc = new SubClassification();
                sc.setName(name);
                sc.setClassification(cls);
                sc.setLogo((String) d[2]);
                sc.setStatus(SubClassification.Status.ACTIVE);
                subClassificationRepository.save(sc);
                log.info("Seeded sub-classification: {}", name);
            }
        }
    }

    private void seedProfessions() {
        if (professionRepository.count() == 0) {
            String[] professions = {
                "Engineer", "Doctor", "Business", "Teacher", "Lawyer",
                "Architect", "Accountant", "Government Employee", "Retired", "Other"
            };
            for (String name : professions) {
                Profession p = new Profession();
                p.setName(name);
                p.setStatus(Profession.Status.ACTIVE);
                professionRepository.save(p);
            }
            log.info("Seeded 10 professions");
        }
    }
}
