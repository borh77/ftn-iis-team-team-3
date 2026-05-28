package com.example.iisdrugcrm.domain.portfolio;

import jakarta.persistence.*;

@Entity
@Table(name = "therapeutic_areas")
public class TherapeuticArea extends AbstractAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EntityStatus status = EntityStatus.ACTIVE;

    protected TherapeuticArea() {
    }

    public TherapeuticArea(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = EntityStatus.ACTIVE;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void archive() {
        this.status = EntityStatus.ARCHIVED;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public EntityStatus getStatus() {
        return status;
    }
}
