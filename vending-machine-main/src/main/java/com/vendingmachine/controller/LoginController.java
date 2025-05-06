package com.vendingmachine.controller;

import com.vendingmachine.service.UserService;
import com.vendingmachine.model.User;
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
            User user = userService.authenticate(usernameText, password);
            if (user != null) {
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
        } catch (SQLException e) {
            System.err.println("Lỗi xác thực: " + e.getMessage());
            showError("Lỗi kết nối database: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        showError("Chức năng đăng ký chưa được implement!");
    }

    private void showError(String message) {
        errorMessageLabel.setText(message);
    }
}