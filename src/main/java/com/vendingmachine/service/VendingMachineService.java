package com.vendingmachine.service;

import com.vendingmachine.database.DatabaseConnection;
import com.vendingmachine.model.VendingMachine;
import java.sql.*;

public class VendingMachineService {
    private static final String MACHINE_CODE = "VM001"; // Mã máy mặc định
    
    public VendingMachine getMachineInfo() throws SQLException {
        String sql = "SELECT * FROM vending_machine WHERE machine_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, MACHINE_CODE);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new VendingMachine(
                    rs.getInt("id"),
                    rs.getString("machine_code"),
                    rs.getString("location"),
                    rs.getDouble("current_balance"),
                    rs.getTimestamp("last_maintained"),
                    rs.getString("status")
                );
            }
        }
        return null;
    }
    
    public void updateBalance(double amount) throws SQLException {
        String sql = "UPDATE vending_machine SET current_balance = current_balance + ? WHERE machine_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Kiểm tra số tiền trong máy sau khi cập nhật không được âm
            double currentBalance = getCurrentBalance();
            if (currentBalance + amount < 0) {
                throw new SQLException("Không đủ tiền trong máy để thực hiện giao dịch");
            }
            
            stmt.setDouble(1, amount);
            stmt.setString(2, MACHINE_CODE);
            stmt.executeUpdate();
        }
    }
    
    public double getCurrentBalance() throws SQLException {
        String sql = "SELECT current_balance FROM vending_machine WHERE machine_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, MACHINE_CODE);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("current_balance");
            }
        }
        return 0.0;
    }

    public void withdrawMoney(double amount) throws SQLException {
        if (amount <= 0) {
            throw new SQLException("Số tiền rút phải lớn hơn 0");
        }

        // Kiểm tra số dư hiện tại
        double currentBalance = getCurrentBalance();
        if (amount > currentBalance) {
            throw new SQLException("Số tiền rút vượt quá số dư trong máy");
        }

        // Thực hiện rút tiền bằng cách trừ số dư
        updateBalance(-amount);
    }
}
