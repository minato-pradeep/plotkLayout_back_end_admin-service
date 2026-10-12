package com.plotvista.admin.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "service_provider")
@Data
public class ServiceProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID companyId;

    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderType type = ProviderType.INDIVIDUAL;

    @Column(nullable = false)
    private String name;

    @ElementCollection
    @CollectionTable(name = "service_provider_work_types",
            joinColumns = @JoinColumn(name = "service_provider_id"))
    @Column(name = "work_types")
    private List<String> workTypes;

    @ElementCollection
    @CollectionTable(name = "service_provider_phones",
            joinColumns = @JoinColumn(name = "service_provider_id"))
    @Column(name = "phones")
    private List<String> phones;

    @ElementCollection
    @CollectionTable(name = "service_provider_addresses",
            joinColumns = @JoinColumn(name = "service_provider_id"))
    @Column(name = "addresses", length = 500)
    private List<String> addresses;

    private String photo;

    private String logo;

    private String locationQuery;

    private int radius = 10;

    private boolean verified = false;

    private boolean active = true;

    @Column(length = 1000)
    private String description;

    private int experience;

    private String workingHours;

    private double rating = 0.0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum ProviderType {
        INDIVIDUAL, COMPANY;

        @JsonCreator
        public static ProviderType fromString(String value) {
            if (value == null) return INDIVIDUAL;
            return ProviderType.valueOf(value.toUpperCase());
        }

        @JsonValue
        public String toJson() {
            return this.name().toLowerCase();
        }
    }
}
