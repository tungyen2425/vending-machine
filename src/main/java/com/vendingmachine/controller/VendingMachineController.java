package com.vendingmachine.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.animation.TranslateTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import com.vendingmachine.database.DatabaseConnection;

public class VendingMachineController {
    @FXML private Label totalPriceLabel;
    @FXML private ListView<String> cartListView;
    @FXML private Label balanceLabel;
    @FXML private FlowPane productsFlowPane;
    
    private double balance = 0.0;
    private double totalPrice = 0.0;
    private ObservableList<String> cartItems = FXCollections.observableArrayList();
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("vi", "VN"));
    private Map<String, Integer> productIdMap = new HashMap<>();
    private Map<String, Integer> currentCart = new HashMap<>();

    @FXML
    public void initialize() {
        cartListView.setItems(cartItems);
        updateBalanceDisplay();
        loadProductsFromDatabase();
        setupButtonAnimations();
        setupDropShadows();
    }

    private void loadProductsFromDatabase() {
        String sql = "SELECT * FROM products ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            productsFlowPane.getChildren().clear();
            
            while (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                String imageUrl = rs.getString("image_url");
                int id = rs.getInt("id");
                
                productIdMap.put(name, id);
                VBox productBox = createProductBox(name, price, imageUrl);
                productsFlowPane.getChildren().add(productBox);
            }
        } catch (SQLException e) {
            showError("Lỗi khi tải danh sách sản phẩm: " + e.getMessage());
        }
    }

    private VBox createProductBox(String name, double price, String imageUrl) {
        VBox productBox = new VBox();
        productBox.setAlignment(javafx.geometry.Pos.CENTER);
        productBox.getStyleClass().add("product-box");
        productBox.setPadding(new Insets(10));

        ImageView imageView = new ImageView();
        imageView.setFitHeight(80);
        imageView.setFitWidth(80);
        imageView.setPreserveRatio(true);
        try {
            imageView.setImage(new Image(getClass().getResourceAsStream("/com/vendingmachine/images/" + imageUrl)));
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/com/vendingmachine/images/default.png")));
        }

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setWrapText(true);
        VBox.setMargin(nameLabel, new Insets(8, 0, 0, 0));

        Label priceLabel = new Label(currencyFormat.format(price));
        priceLabel.getStyleClass().add("product-price");
        VBox.setMargin(priceLabel, new Insets(5));

        Button actionButton;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT quantity FROM products WHERE id = ?")) {
            
            stmt.setInt(1, productIdMap.get(name));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getInt("quantity") > 0) {
                actionButton = new Button("Chọn");
                actionButton.getStyleClass().add("product-button");
                actionButton.setOnAction(e -> handleProductSelection(name, price));
            } else {
                actionButton = new Button("Hết hàng");
                actionButton.getStyleClass().addAll("product-button", "out-of-stock");
                actionButton.setDisable(true);
            }
        } catch (SQLException e) {
            actionButton = new Button("Lỗi");
            actionButton.setDisable(true);
            showError("Lỗi khi kiểm tra số lượng: " + e.getMessage());
        }
        
        VBox.setMargin(actionButton, new Insets(5, 0, 0, 0));
        productBox.getChildren().addAll(imageView, nameLabel, priceLabel, actionButton);
        return productBox;
    }

    private void setupButtonAnimations() {
        findProductBoxes().forEach(productBox -> {
            productBox.setOnMouseEntered(_ -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(100), productBox);
                st.setToX(1.05);
                st.setToY(1.05);
                st.play();
            });
            
            productBox.setOnMouseExited(_ -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(100), productBox);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });
        });
    }

    private void setupDropShadows() {
        DropShadow productShadow = new DropShadow();
        productShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        productShadow.setRadius(10);
        productShadow.setOffsetY(3);
        
        DropShadow cartShadow = new DropShadow();
        cartShadow.setColor(Color.rgb(0, 0, 0, 0.1));
        cartShadow.setRadius(5);
        cartShadow.setOffsetX(-5);
        
        findProductBoxes().forEach(box -> box.setEffect(productShadow));
        
        Node cartPanel = cartListView.getParent();
        while (cartPanel != null && !(cartPanel instanceof VBox)) {
            cartPanel = cartPanel.getParent();
        }
        if (cartPanel != null) {
            cartPanel.setEffect(cartShadow);
        }
    }

    private ObservableList<VBox> findProductBoxes() {
        ObservableList<VBox> productBoxes = FXCollections.observableArrayList();
        for (Node node : productsFlowPane.getChildren()) {
            if (node instanceof VBox && node.getStyleClass().contains("product-box")) {
                productBoxes.add((VBox) node);
            }
        }
        return productBoxes;
    }

    private double parsePrice(String priceText) {
        return Double.parseDouble(priceText.replaceAll("[^\\d]", ""));
    }

    @FXML
    private void handleDeposit() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nạp tiền");
        dialog.setHeaderText("Nhập số tiền muốn nạp");
        dialog.setContentText("Số tiền (VNĐ):");
        
        dialog.showAndWait().ifPresent(amount -> {
            try {
                double depositAmount = Double.parseDouble(amount);
                if (depositAmount > 0) {
                    balance += depositAmount;
                    updateBalanceDisplay();
                    showNotification("Nạp tiền thành công", "Số dư: " + currencyFormat.format(balance));
                }
            } catch (NumberFormatException e) {
                showError("Số tiền không hợp lệ");
            }
        });
    }

    public void handleProductSelection(String productName, double price) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT quantity FROM products WHERE id = ?")) {
            
            int productId = productIdMap.get(productName);
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int availableQuantity = rs.getInt("quantity");
                int currentQuantity = currentCart.getOrDefault(productName, 0);
                
                if (availableQuantity <= 0) {
                    showError("Sản phẩm đã hết hàng!");
                    return;
                }
                
                if (currentQuantity >= availableQuantity) {
                    showError("Số lượng trong kho không đủ!");
                    return;
                }
                
                if (balance >= price) {
                    balance -= price;
                    totalPrice += price;
                    cartItems.add(productName + " - " + currencyFormat.format(price));
                    currentCart.merge(productName, 1, Integer::sum);
                    updateBalanceDisplay();
                    updateTotalPrice();
                } else {
                    showError("Số dư không đủ. Vui lòng nạp thêm tiền.");
                }
            }
        } catch (SQLException e) {
            showError("Lỗi khi kiểm tra số lượng: " + e.getMessage());
        }
    }

    @FXML
    private void handlePayment() {
        if (!cartItems.isEmpty()) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                 
                    String updateProductSql = "UPDATE products SET quantity = quantity - ? WHERE id = ?";
                    String insertTransactionSql = "INSERT INTO transactions (product_id, quantity, total_price) VALUES (?, ?, ?)";
                    
                    PreparedStatement updateProductStmt = conn.prepareStatement(updateProductSql);
                    PreparedStatement insertTransactionStmt = conn.prepareStatement(insertTransactionSql);

                    for (Map.Entry<String, Integer> entry : currentCart.entrySet()) {
                        String productName = entry.getKey();
                        int quantity = entry.getValue();
                        int productId = productIdMap.get(productName);
                        double productPrice = parseProductPrice(productName);
                        
                        // Cập nhật số lượng
                        updateProductStmt.setInt(1, quantity);
                        updateProductStmt.setInt(2, productId);
                        updateProductStmt.executeUpdate();
                        
                        // Thêm giao dịch
                        insertTransactionStmt.setInt(1, productId);
                        insertTransactionStmt.setInt(2, quantity);
                        insertTransactionStmt.setDouble(3, productPrice * quantity);
                        insertTransactionStmt.executeUpdate();
                    }
                    
                    conn.commit();
                    showNotification("Thanh toán thành công", "Tổng tiền: " + currencyFormat.format(totalPrice));
                    
                    // Reset cart
                    cartItems.clear();
                    currentCart.clear();
                    totalPrice = 0.0;
                    updateTotalPrice();
                    
                    // Reload products để cập nhật số lượng hiển thị
                    loadProductsFromDatabase();
                } catch (SQLException e) {
                    conn.rollback();
                    showError("Lỗi khi thanh toán: " + e.getMessage());
                }
            } catch (SQLException e) {
                showError("Lỗi kết nối database: " + e.getMessage());
            }
        }
    }

    private double parseProductPrice(String productName) {
        String priceText = cartItems.stream()
            .filter(item -> item.startsWith(productName))
            .findFirst()
            .map(item -> item.split(" - ")[1])
            .orElse("0 VNĐ");
        return Double.parseDouble(priceText.replaceAll("[^\\d]", ""));
    }

    @FXML
    private void handleCancel() {
        if (!cartItems.isEmpty()) {
            balance += totalPrice;
            updateBalanceDisplay();
            cartItems.clear();
            currentCart.clear();
            totalPrice = 0.0;
            updateTotalPrice();
        }
    }

    @FXML
    private void handleAdminLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/vendingmachine/fxml/login.fxml"));
            Parent loginPage = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Đăng nhập Admin");
            Scene scene = new Scene(loginPage);
            stage.setScene(scene);
            
            // Đóng cửa sổ hiện tại
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
            
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Đường dẫn FXML: " + getClass().getResource("/com/vendingmachine/fxml/login.fxml"));
            showError("Không thể mở trang đăng nhập: " + e.getMessage());
        }
    }

    private void updateBalanceDisplay() {
        balanceLabel.setText(currencyFormat.format(balance));
    }

    private void updateTotalPrice() {
        totalPriceLabel.setText(currencyFormat.format(totalPrice));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}