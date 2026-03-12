package ru.yourteam.lab.domain;

import java.time.Instant;

public class StockMove implements Identifiable {
    private long id;
    private long batchId;
    private StockMoveType type;
    private double quantity;
    private BatchUnit unit;
    private String reason;
    private String ownerUsername = "SYSTEM";
    private Instant movedAt;
    private Instant createdAt;

    public StockMove() {}

    // Геттеры и сеттеры
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getBatchId() { return batchId; }
    public void setBatchId(long batchId) { this.batchId = batchId; }

    public StockMoveType getType() { return type; }
    public void setType(StockMoveType type) { this.type = type; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public BatchUnit getUnit() { return unit; }
    public void setUnit(BatchUnit unit) { this.unit = unit; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public Instant getMovedAt() { return movedAt; }
    public void setMovedAt(Instant movedAt) { this.movedAt = movedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}