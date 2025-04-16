package com.vendingmachine.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import com.vendingmachine.database.DatabaseConnection;

public class OrderHistoryController {
    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, Integer> idColumn;
    @FXML private TableColumn<Order, String> productColumn;
    @FXML private TableColumn<Order, Integer> quantityColumn;
    @FXML private TableColumn<Order, Double> totalPriceColumn;
    @FXML private TableColumn<Order, Timestamp> dateColumn;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private Label totalRevenueLabel;

    private ObservableList<Order> orders = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        loadOrders();
    }

    private void setupColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        
        // Format price column
        totalPriceColumn.setCellFactory(column -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f VNĐ", price));
                }
            }
        });

        // Format date column
        dateColumn.setCellFactory(column -> new TableCell<Order, Timestamp>() {
            @Override
            protected void updateItem(Timestamp date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.toString());
                }
            }
        });
    }

    @FXML
    private void handleFilter() {
        LocalDate start = fromDate.getValue();
        LocalDate end = toDate.getValue();
        loadOrders(start, end);
    }

    private void loadOrders() {
        loadOrders(null, null);
    }

    private void loadOrders(LocalDate fromDate, LocalDate toDate) {
        orders.clear();
        String sql = "SELECT t.id, p.name, t.quantity, t.total_price, t.transaction_date " +
                    "FROM transactions t " +
                    "JOIN products p ON t.product_id = p.id ";
        
        if (fromDate != null && toDate != null) {
            sql += "WHERE DATE(t.transaction_date) BETWEEN ? AND ? ";
        }
        sql += "ORDER BY t.transaction_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (fromDate != null && toDate != null) {
                stmt.setDate(1, Date.valueOf(fromDate));
                stmt.setDate(2, Date.valueOf(toDate));
            }

            ResultSet rs = stmt.executeQuery();
            double totalRevenue = 0;

            while (rs.next()) {
                Order order = new Order(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("quantity"),
                    rs.getDouble("total_price"),
                    rs.getTimestamp("transaction_date")
                );
                orders.add(order);
                totalRevenue += order.getTotalPrice();
            }

            orderTable.setItems(orders);
            totalRevenueLabel.setText(String.format("%,.0f VNĐ", totalRevenue));

        } catch (SQLException e) {
            showError("Lỗi khi tải lịch sử đơn hàng: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vendingmachine/fxml/admin_products.fxml"));
            Parent adminPage = loader.load();
            Scene scene = new Scene(adminPage);
            scene.getStylesheets().add(getClass().getResource("/com/vendingmachine/css/admin.css").toExternalForm());
            
            Stage stage = (Stage) orderTable.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            showError("Không thể quay lại trang quản lý: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Order {
        private final int id;
        private final String productName;
        private final int quantity;
        private final double totalPrice;
        private final Timestamp transactionDate;

        public Order(int id, String productName, int quantity, double totalPrice, Timestamp transactionDate) {
            this.id = id;
            this.productName = productName;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
            this.transactionDate = transactionDate;
        }

        public int getId() { return id; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public double getTotalPrice() { return totalPrice; }
        public Timestamp getTransactionDate() { return transactionDate; }
    }
}