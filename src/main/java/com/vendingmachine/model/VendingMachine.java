package com.vendingmachine.model;

import java.sql.Timestamp;

public class VendingMachine {
    private int id;
    private String machineCode;
    private String location;
    private double currentBalance;
    private Timestamp lastMaintained;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public VendingMachine(int id, String machineCode, String location, double currentBalance, 
                         Timestamp lastMaintained, String status) {
        this.id = id;
        this.machineCode = machineCode;
        this.location = location;
        this.currentBalance = currentBalance;
        this.lastMaintained = lastMaintained;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMachineCode() { return machineCode; }
    public void setMachineCode(String machineCode) { this.machineCode = machineCode; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(double currentBalance) { this.currentBalance = currentBalance; }

    public Timestamp getLastMaintained() { return lastMaintained; }
    public void setLastMaintained(Timestamp lastMaintained) { this.lastMaintained = lastMaintained; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}