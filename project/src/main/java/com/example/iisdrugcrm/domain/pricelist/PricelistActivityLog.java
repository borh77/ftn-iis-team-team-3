package com.example.iisdrugcrm.domain.pricelist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pricelist_activity_logs")
public class PricelistActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pricelist_id", nullable = false)
    private Long pricelistId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "team_id")
    private Long teamId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 64)
    private PricelistActionType actionType;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    protected PricelistActivityLog() {
    }

    public PricelistActivityLog(Long pricelistId, Long userId, Long teamId, PricelistActionType actionType, String description, OffsetDateTime timestamp) {
        this.pricelistId = pricelistId;
        this.userId = userId;
        this.teamId = teamId;
        this.actionType = actionType;
        this.description = description;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public Long getPricelistId() {
        return pricelistId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public PricelistActionType getActionType() {
        return actionType;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}
