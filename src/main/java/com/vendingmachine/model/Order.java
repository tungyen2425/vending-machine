package com.vendingmachine.model;

import java.sql.Timestamp;

public class Order {
    private int id;
    private int productId;
    private String productName;
    private int quantity;
    private double totalPrice;
    private Timestamp transactionDate;

    public Order() {}

    public Order(int id, int productId, String productName, int quantity, double totalPrice, Timestamp transactionDate) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.transactionDate = transactionDate;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName != null ? productName : "Sản phẩm đã xóa"; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public Timestamp getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Timestamp transactionDate) { this.transactionDate = transactionDate; }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", totalPrice=" + totalPrice +
                ", transactionDate=" + transactionDate +
                '}';
    }
}