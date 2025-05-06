package com.vendingmachine.controller;

import com.vendingmachine.service.OrderService;
import com.vendingmachine.model.Order;
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
import java.util.List;

public class OrderHistoryController {
    @FXML private TableView<OrderDisplay> orderTable;
    @FXML private TableColumn<OrderDisplay, Integer> idColumn;
    @FXML private TableColumn<OrderDisplay, String> productColumn;
    @FXML private TableColumn<OrderDisplay, Integer> quantityColumn;
    @FXML private TableColumn<OrderDisplay, Double> totalPriceColumn;
    @FXML private TableColumn<OrderDisplay, Timestamp> dateColumn;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private Label totalRevenueLabel;

    private final OrderService orderService = new OrderService();
    private ObservableList<OrderDisplay> orders = FXCollections.observableArrayList();

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
        totalPriceColumn.setCellFactory(_ -> new TableCell<OrderDisplay, Double>() {
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
        dateColumn.setCellFactory(_ -> new TableCell<OrderDisplay, Timestamp>() {
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
        
        if (start != null && end != null) {
            try {
                List<Order> orderList = orderService.getOrdersByDateRange(start, end);
                orders.clear();
                for (Order order : orderList) {
                    orders.add(new OrderDisplay(
                        order.getId(),
                        getProductName(order.getProductId()),
                        order.getQuantity(),
                        order.getTotalPrice(),
                        order.getTransactionDate()
                    ));
                }
                orderTable.setItems(orders);
                updateTotalRevenue(start, end);
            } catch (SQLException e) {
                showError("Lỗi khi tải lịch sử đơn hàng: " + e.getMessage());
            }
        } else {
            showError("Vui lòng chọn khoảng thời gian");
        }
    }

    private String getProductName(int productId) throws SQLException {
        try (Connection conn = com.vendingmachine.database.DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM products WHERE id = ?")) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        }
        return "Unknown Product";
    }

    private void loadOrders() {
        LocalDate today = LocalDate.now();
        fromDate.setValue(today.minusDays(30));
        toDate.setValue(today);
        handleFilter();
    }

    private void updateTotalRevenue(LocalDate fromDate, LocalDate toDate) {
        try {
            double totalRevenue = orderService.getTotalRevenue(fromDate, toDate);
            totalRevenueLabel.setText(String.format("%,.0f VNĐ", totalRevenue));
        } catch (SQLException e) {
            showError("Lỗi khi tính tổng doanh thu: " + e.getMessage());
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

    // Inner class for display purposes
    public static class OrderDisplay {
        private final int id;
        private final String productName;
        private final int quantity;
        private final double totalPrice;
        private final Timestamp transactionDate;

        public OrderDisplay(int id, String productName, int quantity, double totalPrice, Timestamp transactionDate) {
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