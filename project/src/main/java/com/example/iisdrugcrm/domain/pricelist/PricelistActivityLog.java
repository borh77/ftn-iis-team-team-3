package com.example.iisdrugcrm.domain.pricelist;

import com.example.iisdrugcrm.domain.PricelistStatus;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status_from", length = 32)
    private PricelistStatus statusFrom;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_to", length = 32)
    private PricelistStatus statusTo;

    protected PricelistActivityLog() {
    }

    public PricelistActivityLog(Long pricelistId, Long userId, Long teamId, PricelistActionType actionType, String description, OffsetDateTime timestamp) {
        this(pricelistId, userId, teamId, actionType, description, timestamp, null, null);
    }

    public PricelistActivityLog(
            Long pricelistId,
            Long userId,
            Long teamId,
            PricelistActionType actionType,
            String description,
            OffsetDateTime timestamp,
            PricelistStatus statusFrom,
            PricelistStatus statusTo
    ) {
        this.pricelistId = pricelistId;
        this.userId = userId;
        this.teamId = teamId;
        this.actionType = actionType;
        this.description = description;
        this.timestamp = timestamp;
        this.statusFrom = statusFrom;
        this.statusTo = statusTo;
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

    public PricelistStatus getStatusFrom() {
        return statusFrom;
    }

    public PricelistStatus getStatusTo() {
        return statusTo;
    }
}
