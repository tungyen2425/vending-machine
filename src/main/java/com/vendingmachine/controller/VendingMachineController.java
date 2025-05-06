package com.vendingmachine.controller;

import com.vendingmachine.service.ProductService;
import com.vendingmachine.service.OrderService;
import com.vendingmachine.service.VendingMachineService;
import com.vendingmachine.model.Product;
import com.vendingmachine.model.Order;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class VendingMachineController {
    @FXML private Label totalPriceLabel;
    @FXML private ListView<String> cartListView;
    @FXML private Label balanceLabel;
    @FXML private FlowPane productsFlowPane;
    
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private final VendingMachineService vendingMachineService = new VendingMachineService();
    
    private double balance = 0.0; // Số dư của khách hàng
    private double totalPrice = 0.0;
    private ObservableList<String> cartItems = FXCollections.observableArrayList();
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("vi", "VN"));
    private Map<String, Product> productMap = new HashMap<>();
    private Map<String, Integer> currentCart = new HashMap<>();
    private Map<String, Integer> productIdMap = new HashMap<>();

    @FXML
    public void initialize() {
        cartListView.setItems(cartItems);
        updateBalanceDisplay();
        loadProducts();
    }

    private void loadProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            productsFlowPane.getChildren().clear();
            
            for (Product product : products) {
                productMap.put(product.getName(), product);
                productIdMap.put(product.getName(), Integer.parseInt(product.getId()));
                VBox productBox = createProductBox(product);
                productsFlowPane.getChildren().add(productBox);
            }
        } catch (SQLException e) {
            showError("Lỗi khi tải danh sách sản phẩm: " + e.getMessage());
        }
    }

    private VBox createProductBox(Product product) {
        VBox productBox = new VBox();
        productBox.setAlignment(javafx.geometry.Pos.CENTER);
        productBox.getStyleClass().add("product-box");
        productBox.setPadding(new Insets(10));

        ImageView imageView = new ImageView();
        imageView.setFitHeight(80);
        imageView.setFitWidth(80);
        imageView.setPreserveRatio(true);
        try {
            imageView.setImage(new Image(getClass().getResourceAsStream("/com/vendingmachine/images/" + product.getImagePath())));
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/com/vendingmachine/images/default.png")));
        }

        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setWrapText(true);

        Label priceLabel = new Label(currencyFormat.format(product.getPrice()));
        priceLabel.getStyleClass().add("product-price");

        Button actionButton;
        if (product.getQuantity() > 0) {
            actionButton = new Button("Chọn");
            actionButton.getStyleClass().add("product-button");
            actionButton.setOnAction(_ -> handleProductSelection(product));
        } else {
            actionButton = new Button("Hết hàng");
            actionButton.getStyleClass().addAll("product-button", "out-of-stock");
            actionButton.setDisable(true);
        }

        productBox.getChildren().addAll(imageView, nameLabel, priceLabel, actionButton);
        return productBox;
    }

    @FXML
    private void handleDeposit() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nạp tiền");
        dialog.setHeaderText("Nhập số tiền muốn nạp");
        dialog.setContentText("Số tiền (VNĐ):");
        
        dialog.showAndWait().ifPresent(amount -> {
            double depositAmount = Double.parseDouble(amount);
            try {
                if (depositAmount > 0) {
                    // Cập nhật số dư của khách hàng
                    balance += depositAmount;
                    // Cập nhật số tiền trong máy
                    vendingMachineService.updateBalance(depositAmount);
                    updateBalanceDisplay();
                    showNotification("Nạp tiền thành công", "Số dư: " + currencyFormat.format(balance));
                } else {
                    showError("Số tiền phải lớn hơn 0");
                }
            } catch (NumberFormatException e) {
                showError("Số tiền không hợp lệ");
            } catch (SQLException e) {
                showError("Lỗi khi cập nhật số dư: " + e.getMessage());
                // Rollback balance nếu có lỗi
                balance -= depositAmount;
                updateBalanceDisplay();
            }
        });
    }

    @FXML
    private void handleWithdrawBalance() {
        if (balance > 0) {
            try {
                // Kiểm tra số dư trong máy
                double machineBalance = vendingMachineService.getCurrentBalance();
                if (machineBalance < balance) {
                    showError("Máy tạm thời không đủ tiền. Vui lòng thông báo cho nhân viên.");
                    return;
                }
                
                vendingMachineService.updateBalance(-balance);
                showNotification("Lấy tiền thành công", "Số tiền: " + currencyFormat.format(balance));
                balance = 0;
                updateBalanceDisplay();
            } catch (SQLException e) {
                showError("Lỗi khi lấy tiền: " + e.getMessage());
            }
        } else {
            showNotification("Thông báo", "Không có tiền để lấy");
        }
    }

    private void handleProductSelection(Product product) {
        try {
            Product currentProduct = productService.getProductById(product.getId());
            if (currentProduct == null || currentProduct.getQuantity() <= 0) {
                showError("Sản phẩm đã hết hàng!");
                return;
            }
            
            int currentQuantity = currentCart.getOrDefault(product.getName(), 0);
            if (currentQuantity >= currentProduct.getQuantity()) {
                showError("Số lượng trong kho không đủ!");
                return;
            }
            
            if (balance >= product.getPrice()) {
                balance -= product.getPrice();
                totalPrice += product.getPrice();
                cartItems.add(product.getName() + " - " + currencyFormat.format(product.getPrice()));
                currentCart.merge(product.getName(), 1, Integer::sum);
                updateBalanceDisplay();
                updateTotalPrice();
            } else {
                showError("Số dư không đủ. Vui lòng nạp thêm tiền.");
            }
        } catch (SQLException e) {
            showError("Lỗi khi kiểm tra sản phẩm: " + e.getMessage());
        }
    }

    @FXML
    private void handlePayment() {
        if (!cartItems.isEmpty()) {
            try {
                // Create orders and update product quantities
                for (Map.Entry<String, Integer> entry : currentCart.entrySet()) {
                    String productName = entry.getKey();
                    int quantity = entry.getValue();
                    int productId = productIdMap.get(productName);
                    double productPrice = parseProductPrice(productName);
                    
                    // Update product quantity
                    productService.updateQuantity(String.valueOf(productId), quantity);
                    
                    // Create order record
                    Order order = new Order(
                        0,
                        productId,
                        quantity,
                        productPrice * quantity,
                        new Timestamp(System.currentTimeMillis())
                    );
                    orderService.createOrder(order);
                }
                
                showNotification("Thanh toán thành công", "Tổng tiền: " + currencyFormat.format(totalPrice));
                
                // Reset cart
                cartItems.clear();
                currentCart.clear();
                totalPrice = 0.0;
                updateTotalPrice();
                
                // Reload products
                loadProductsFromDatabase();
            } catch (SQLException e) {
                showError("Lỗi khi thanh toán: " + e.getMessage());
            }
        }
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
            
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
            stage.show();
        } catch (IOException e) {
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

    private double parseProductPrice(String productName) {
        Product product = productMap.get(productName);
        return product != null ? product.getPrice() : 0.0;
    }

    private void loadProductsFromDatabase() {
        loadProducts();
    }
}