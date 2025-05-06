package com.vendingmachine.service;

import com.vendingmachine.database.DatabaseConnection;
import com.vendingmachine.model.Order;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderService {
    public void createOrder(Order order) throws SQLException {
        String sql = "INSERT INTO transactions (product_id, quantity, total_price, transaction_date) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, order.getProductId());
            stmt.setInt(2, order.getQuantity());
            stmt.setDouble(3, order.getTotalPrice());
            stmt.setTimestamp(4, order.getTransactionDate());
            
            stmt.executeUpdate();
        }
    }

    public List<Order> getOrdersByDateRange(LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE DATE(transaction_date) BETWEEN ? AND ? ORDER BY transaction_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(fromDate));
            stmt.setDate(2, java.sql.Date.valueOf(toDate));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Order order = new Order(
                    rs.getInt("id"),
                    rs.getInt("product_id"),
                    rs.getInt("quantity"),
                    rs.getDouble("total_price"),
                    rs.getTimestamp("transaction_date")
                );
                orders.add(order);
            }
        }
        return orders;
    }

    public double getTotalRevenue(LocalDate fromDate, LocalDate toDate) throws SQLException {
        String sql = "SELECT SUM(total_price) as total FROM transactions WHERE DATE(transaction_date) BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(fromDate));
            stmt.setDate(2, java.sql.Date.valueOf(toDate));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0.0;
    }
}