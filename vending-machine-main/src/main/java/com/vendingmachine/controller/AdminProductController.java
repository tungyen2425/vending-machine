package com.vendingmachine.controller;

import com.vendingmachine.service.ProductService;
import com.vendingmachine.service.VendingMachineService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.nio.file.*;
import java.text.NumberFormat;
import java.util.Locale;
import com.vendingmachine.model.Product;
import com.vendingmachine.database.DatabaseConnection;

public class AdminProductController {
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> quantityColumn;
    @FXML private TableColumn<Product, String> imageColumn;
    @FXML private TableColumn<Product, Void> actionsColumn;
    
    @FXML private Label totalProductsLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label totalValueLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private TextField searchField;
    @FXML private Button logoutButton;
    @FXML private Label machineBalanceLabel;
    @FXML private Button collectMoneyButton;
    
    private ObservableList<Product> products = FXCollections.observableArrayList();
    private FilteredList<Product> filteredProducts;
    private final ProductService productService = new ProductService();
    private final VendingMachineService vendingMachineService = new VendingMachineService();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    
    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("imagePath"));
        
        loadProductsFromDatabase();
        
        filteredProducts = new FilteredList<>(products, _ -> true);
        productTable.setItems(filteredProducts);
        
        searchField.textProperty().addListener((_, _, newValue) -> {
            filteredProducts.setPredicate(product -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return product.getName().toLowerCase().contains(lowerCaseFilter) ||
                       product.getId().toLowerCase().contains(lowerCaseFilter);
            });
            updateStatistics();
        });
        
        setupActionsColumn();
        updateStatistics();
        updateMachineBalance();
    }
    
    private void loadProductsFromDatabase() {
        try {
            products.clear();
            products.addAll(productService.getAllProducts());
        } catch (SQLException e) {
            showError("Lỗi khi tải danh sách sản phẩm: " + e.getMessage());
        }
    }
    
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button editButton = new Button("Sửa");
            private final Button deleteButton = new Button("Xóa");
            
            {
                editButton.setOnAction(_ -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleEditProduct(product);
                });
                
                deleteButton.setOnAction(_ -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleDeleteProduct(product);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
    }

    @FXML
    private void handleAddProduct() {
        Dialog<Product> dialog = createProductDialog("Thêm sản phẩm mới", null);
        dialog.showAndWait().ifPresent(product -> {
            try {
                productService.addProduct(product);
                products.add(product);
                updateStatistics();
            } catch (SQLException e) {
                showError("Lỗi khi thêm sản phẩm: " + e.getMessage());
            }
        });
    }

    private void handleEditProduct(Product product) {
        Dialog<Product> dialog = createProductDialog("Sửa sản phẩm", product);
        dialog.showAndWait().ifPresent(updatedProduct -> {
            try {
                productService.updateProduct(updatedProduct);
                loadProductsFromDatabase(); // Reload to ensure consistency
                updateStatistics();
            } catch (SQLException e) {
                showError("Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            }
        });
    }

    private void handleDeleteProduct(Product product) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận xóa");
        confirmDialog.setHeaderText("Xóa sản phẩm");
        confirmDialog.setContentText("Bạn có chắc muốn xóa sản phẩm này?");

        if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                productService.deleteProduct(product.getId());
                products.remove(product);
                updateStatistics();
            } catch (SQLException e) {
                showError("Lỗi khi xóa sản phẩm: " + e.getMessage());
            }
        }
    }

    private Dialog<Product> createProductDialog(String title, Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(product == null ? "Nhập thông tin sản phẩm mới" : "Cập nhật thông tin sản phẩm");

        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField(product != null ? product.getName() : "");
        TextField priceField = new TextField(product != null ? String.valueOf(product.getPrice()) : "");
        TextField quantityField = new TextField(product != null ? String.valueOf(product.getQuantity()) : "");
        TextField imagePathField = new TextField(product != null ? product.getImagePath() : "");
        Button chooseImageButton = new Button("Chọn ảnh");

        grid.add(new Label("Tên sản phẩm:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Giá:"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(new Label("Số lượng:"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(new Label("Hình ảnh:"), 0, 3);
        grid.add(imagePathField, 1, 3);
        grid.add(chooseImageButton, 2, 3);

        chooseImageButton.setOnAction(_ -> handleImageSelection(imagePathField));

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    validateProductFields(nameField.getText(), priceField.getText(), quantityField.getText());
                    return new Product(
                        product != null ? product.getId() : "0",
                        nameField.getText(),
                        Double.parseDouble(priceField.getText()),
                        Integer.parseInt(quantityField.getText()),
                        imagePathField.getText().isEmpty() ? "default.png" : imagePathField.getText()
                    );
                } catch (IllegalArgumentException ex) {
                    showError(ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    private void handleImageSelection(TextField imagePathField) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(imagePathField.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Path sourcePath = selectedFile.toPath();
                Path targetPath = Path.of("src/main/resources/com/vendingmachine/images/" + selectedFile.getName());
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                imagePathField.setText(selectedFile.getName());
            } catch (IOException ex) {
                showError("Lỗi khi lưu hình ảnh: " + ex.getMessage());
            }
        }
    }

    private void validateProductFields(String name, String price, String quantity) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống!");
        }
        
        try {
            double priceValue = Double.parseDouble(price);
            if (priceValue <= 0) {
                throw new IllegalArgumentException("Giá phải lớn hơn 0!");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá không hợp lệ!");
        }
        
        try {
            int quantityValue = Integer.parseInt(quantity);
            if (quantityValue < 0) {
                throw new IllegalArgumentException("Số lượng không được âm!");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Số lượng không hợp lệ!");
        }
    }

    private void updateStatistics() {
        totalProductsLabel.setText(String.valueOf(filteredProducts.size()));
        long lowStockCount = filteredProducts.stream().filter(p -> p.getQuantity() < 10).count();
        lowStockLabel.setText(String.valueOf(lowStockCount));
        
        double totalValue = filteredProducts.stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();
        totalValueLabel.setText(String.format("%,.0f VNĐ", totalValue));

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT SUM(total_price) as total FROM transactions")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double totalRevenue = rs.getDouble("total");
                totalRevenueLabel.setText(String.format("%,.0f VNĐ", totalRevenue));
            }
        } catch (SQLException e) {
            showError("Lỗi khi tính tổng doanh thu: " + e.getMessage());
        }
    }

    private void updateMachineBalance() {
        try {
            double balance = vendingMachineService.getCurrentBalance();
            machineBalanceLabel.setText(currencyFormat.format(balance));
            collectMoneyButton.setDisable(balance <= 0);
        } catch (SQLException e) {
            showError("Lỗi khi lấy thông tin số dư: " + e.getMessage());
        }
    }

    @FXML
    private void handleCollectMoney() {
        try {
            double currentBalance = vendingMachineService.getCurrentBalance();
            if (currentBalance <= 0) {
                showNotification("Thông báo", "Không có tiền để thu");
                return;
            }

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Thu tiền");
            dialog.setHeaderText("Nhập số tiền muốn thu");
            dialog.setContentText("Số tiền (VNĐ):");

            dialog.showAndWait().ifPresent(amount -> {
                try {
                    double collectAmount = Double.parseDouble(amount);
                    if (collectAmount <= 0) {
                        showError("Số tiền phải lớn hơn 0");
                        return;
                    }
                    if (collectAmount > currentBalance) {
                        showError("Số tiền vượt quá số dư hiện tại");
                        return;
                    }

                    double collected = vendingMachineService.collectMoney(collectAmount);
                    updateMachineBalance();
                    showNotification("Thu tiền thành công", 
                        String.format("Đã thu: %s\nSố dư còn lại: %s", 
                            currencyFormat.format(collected),
                            currencyFormat.format(vendingMachineService.getCurrentBalance())));
                } catch (NumberFormatException e) {
                    showError("Số tiền không hợp lệ");
                } catch (SQLException e) {
                    showError("Lỗi khi thu tiền: " + e.getMessage());
                }
            });
        } catch (SQLException e) {
            showError("Lỗi khi kiểm tra số dư: " + e.getMessage());
        }
    }

    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleViewOrders() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vendingmachine/fxml/order_history.fxml"));
            Parent orderHistory = loader.load();
            Scene scene = new Scene(orderHistory);
            scene.getStylesheets().add(getClass().getResource("/com/vendingmachine/css/admin.css").toExternalForm());
            
            Stage stage = (Stage) productTable.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            showError("Không thể mở trang lịch sử đơn hàng: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vendingmachine/fxml/mainpage.fxml"));
            Parent mainPage = loader.load();
            Scene scene = new Scene(mainPage, 900, 680);
            scene.getStylesheets().add(getClass().getResource("/com/vendingmachine/css/mainpage.css").toExternalForm());
            
            Stage stage = (Stage) productTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Vending Machine");
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            showError("Không thể đăng xuất: " + e.getMessage());
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