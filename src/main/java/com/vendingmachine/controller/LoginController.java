package com.vendingmachine.controller;

import com.vendingmachine.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {
    @FXML private TextField username;
    @FXML private PasswordField userPassword;
    @FXML private Button loginButton;
    @FXML private ProgressIndicator loginProgress;
    @FXML private Label errorMessageLabel;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Hyperlink registerLink;

    private final UserService userService = new UserService();

    @FXML
    private void handleLogin(ActionEvent event) {
        String usernameText = username.getText();
        String password = userPassword.getText();

        try {
            if (userService.authenticate(usernameText, password) != null) {
                openAdminPage(event);
            } else {
                errorMessageLabel.setText("Sai tên đăng nhập hoặc mật khẩu!");
            }
        } catch (SQLException e) {
            showError("Lỗi xác thực: " + e.getMessage());
        }
    }

    private void openAdminPage(ActionEvent event) {
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
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        // TODO: Implement registration logic
    }

    private void showError(String message) {
        errorMessageLabel.setText(message);
    }
}