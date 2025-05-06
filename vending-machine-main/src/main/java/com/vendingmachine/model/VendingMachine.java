package com.vendingmachine.model;

import java.sql.Timestamp;

public class VendingMachine {
    private int id;
    private double currentBalance;
    private Timestamp lastCollectionDate;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public VendingMachine(int id, double currentBalance, Timestamp lastCollectionDate, String status) {
        this.id = id;
        this.currentBalance = currentBalance;
        this.lastCollectionDate = lastCollectionDate;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(double currentBalance) { this.currentBalance = currentBalance; }

    public Timestamp getLastCollectionDate() { return lastCollectionDate; }
    public void setLastCollectionDate(Timestamp lastCollectionDate) { this.lastCollectionDate = lastCollectionDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}