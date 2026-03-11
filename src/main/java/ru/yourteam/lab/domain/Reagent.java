package ru.yourteam.lab.domain;

import java.time.Instant;

public class Reagent {
    private long id;
    private String name;
    private String formula;
    private String cas;
    private String hazardClass;
    private String ownerUsername = "SYSTEM"; // По ТЗ на ранних этапах "SYSTEM"
    private Instant createdAt;
    private Instant updatedAt;

    // Пустой конструктор для удобства
    public Reagent() {}

    // Геттеры и сеттеры
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFormula() { return formula; }
    public void setFormula(String formula) { this.formula = formula; }

    public String getCas() { return cas; }
    public void setCas(String cas) { this.cas = cas; }

    public String getHazardClass() { return hazardClass; }
    public void setHazardClass(String hazardClass) { this.hazardClass = hazardClass; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}