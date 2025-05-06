package com.vendingmachine.service;

import com.vendingmachine.database.DatabaseConnection;
import com.vendingmachine.model.VendingMachine;
import java.sql.*;

public class VendingMachineService {
    private static final int MACHINE_ID = 1;

    public VendingMachine getMachineInfo() throws SQLException {
        String sql = "SELECT * FROM vendingmachine WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, MACHINE_ID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new VendingMachine(
                    rs.getInt("id"),
                    rs.getDouble("current_balance"),
                    rs.getTimestamp("last_collection_date"),
                    rs.getString("status")
                );
            }
        }
        return null;
    }

    public void updateBalance(double amount) throws SQLException {
        // Kiểm tra số dư hiện tại nếu là thao tác rút tiền
        if (amount < 0) {
            VendingMachine machine = getMachineInfo();
            if (machine == null || machine.getCurrentBalance() + amount < 0) {
                throw new SQLException("Số tiền trong máy không đủ để thực hiện giao dịch");
            }
        }

        String sql = "UPDATE vendingmachine SET current_balance = current_balance + ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, amount);
            stmt.setInt(2, MACHINE_ID);
            int updated = stmt.executeUpdate();
            
            if (updated == 0) {
                throw new SQLException("Không thể cập nhật số tiền trong máy");
            }
        }
    }

    public double collectMoney(double amount) throws SQLException {
        VendingMachine machine = getMachineInfo();
        if (machine == null || machine.getCurrentBalance() < amount) {
            throw new SQLException("Không đủ tiền để rút");
        }

        String sql = "UPDATE vendingmachine SET current_balance = current_balance - ?, last_collection_date = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, amount);
            stmt.setInt(2, MACHINE_ID);
            int updated = stmt.executeUpdate();
            
            if (updated > 0) {
                return amount;
            }
            throw new SQLException("Không thể rút tiền");
        }
    }

    public double getCurrentBalance() throws SQLException {
        VendingMachine machine = getMachineInfo();
        return machine != null ? machine.getCurrentBalance() : 0.0;
    }
}