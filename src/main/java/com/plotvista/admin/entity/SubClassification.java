package com.plotvista.admin.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sub_classifications")
@Data
public class SubClassification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String logo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "classification_id", nullable = false)
    @JsonIgnoreProperties({"subClassifications", "hibernateLazyInitializer", "handler"})
    private Classification classification;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum Status { ACTIVE, INACTIVE }
}
