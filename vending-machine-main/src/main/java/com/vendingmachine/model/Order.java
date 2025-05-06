package com.vendingmachine.model;

import java.time.LocalDateTime;

public class Order {
    private Integer id;
    private Integer productId;
    private String productName;
    private Double price;
    private Integer quantity;
    private Double total;
    private LocalDateTime orderDate;
    private String paymentMethod;

    public Order() {
    }

    public Order(Integer id, Integer productId, String productName, Double price, Integer quantity, Double total, LocalDateTime orderDate, String paymentMethod) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.total = total;
        this.orderDate = orderDate;
        this.paymentMethod = paymentMethod;
    }

    public Order(Integer productId, String productName, Double price, Integer quantity, String paymentMethod) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.total = price * quantity;
        this.orderDate = LocalDateTime.now();
        this.paymentMethod = paymentMethod;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}