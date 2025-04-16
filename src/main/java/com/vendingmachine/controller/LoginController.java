package com.vendingmachine.controller;

import com.vendingmachine.database.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {
    @FXML private TextField username;
    @FXML private PasswordField userPassword;
    @FXML private Button loginButton;
    @FXML private ProgressIndicator loginProgress;
    @FXML private Label errorMessageLabel;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Hyperlink registerLink;

    @FXML
    private void handleLogin(ActionEvent event) {
        String usernameText = username.getText();
        String password = userPassword.getText();

        if (authenticateUser(usernameText, password)) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/com/vendingmachine/fxml/admin_products.fxml"));
                Parent adminPage = loader.load();
                Scene adminScene = new Scene(adminPage);
                adminScene.getStylesheets().add(getClass().getResource("/com/vendingmachine/css/admin.css").toExternalForm());
                Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
                window.setScene(adminScene);
                window.show();
            } catch (IOException e) {
                e.printStackTrace();
                showError("Không thể mở trang admin: " + e.getMessage());
            }
        } else {
            errorMessageLabel.setText("Sai tên đăng nhập hoặc mật khẩu!");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
   
        showError("Chức năng đăng ký chưa được implement!");
    }

    private boolean authenticateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi xác thực: " + e.getMessage());
            showError("Lỗi kết nối database: " + e.getMessage());
            return false;
        }
    }

    private void showError(String message) {
        errorMessageLabel.setText(message);
    }
}