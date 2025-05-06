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

    private final OrderService orderService = new OrderService();
    private ObservableList<Order> orders = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        loadOrders();
    }

    private void setupColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        
        // Format price column
        totalPriceColumn.setCellFactory(_ -> new TableCell<Order, Double>() {
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
        dateColumn.setCellFactory(_ -> new TableCell<Order, Timestamp>() {
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
        
        if (start == null || end == null) {
            showError("Vui lòng chọn khoảng thời gian!");
            return;
        }
        
        try {
            orders.clear();
            orders.addAll(orderService.getOrdersByDateRange(start, end));
            orderTable.setItems(orders);
            
            double totalRevenue = orderService.getTotalRevenue(start, end);
            totalRevenueLabel.setText(String.format("%,.0f VNĐ", totalRevenue));
        } catch (SQLException e) {
            showError("Lỗi khi tải dữ liệu: " + e.getMessage());
        }
    }

    private void loadOrders() {
        try {
            LocalDate today = LocalDate.now();
            orders.clear();
            orders.addAll(orderService.getOrdersByDateRange(today, today));
            orderTable.setItems(orders);
            
            double totalRevenue = orderService.getTotalRevenue(today, today);
            totalRevenueLabel.setText(String.format("%,.0f VNĐ", totalRevenue));
        } catch (SQLException e) {
            showError("Lỗi khi tải dữ liệu: " + e.getMessage());
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
}