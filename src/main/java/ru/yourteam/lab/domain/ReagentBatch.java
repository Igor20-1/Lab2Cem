package ru.yourteam.lab.domain;

import java.time.Instant;

public class ReagentBatch implements Identifiable {
    private long id;
    private long reagentId;
    private String label;
    private double quantityCurrent;
    private BatchUnit unit;
    private String location;
    private Instant expiresAt;
    private BatchStatus status = BatchStatus.ACTIVE;
    private String ownerUsername = "SYSTEM";
    private Instant createdAt;
    private Instant updatedAt;

    public ReagentBatch() {}

    // Геттеры и сеттеры
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getReagentId() { return reagentId; }
    public void setReagentId(long reagentId) { this.reagentId = reagentId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public double getQuantityCurrent() { return quantityCurrent; }
    public void setQuantityCurrent(double quantityCurrent) { this.quantityCurrent = quantityCurrent; }

    public BatchUnit getUnit() { return unit; }
    public void setUnit(BatchUnit unit) { this.unit = unit; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}