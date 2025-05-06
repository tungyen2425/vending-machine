package com.vendingmachine.controller;

import com.vendingmachine.service.ProductService;
import com.vendingmachine.service.VendingMachineService;
import com.vendingmachine.model.Product;
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
import java.nio.file.*;
import java.sql.SQLException;

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
    @FXML private Label machineBalance;
    @FXML private TextField searchField;
    @FXML private Button withdrawButton;
    
    private final ProductService productService = new ProductService();
    private final VendingMachineService vendingMachineService = new VendingMachineService();
    private ObservableList<Product> products = FXCollections.observableArrayList();
    private FilteredList<Product> filteredProducts;
    
    @FXML
    public void initialize() {
        setupTableColumns();
        loadProducts();
        setupSearch();
        setupActionsColumn();
        updateStatistics();
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("imagePath"));
    }
    
    private void loadProducts() {
        try {
            products.clear();
            products.addAll(productService.getAllProducts());
            filteredProducts = new FilteredList<>(products, _ -> true);
            productTable.setItems(filteredProducts);
        } catch (SQLException e) {
            showError("Lỗi khi tải danh sách sản phẩm: " + e.getMessage());
        }
    }
    
    private void setupSearch() {
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
                loadProducts();
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
                loadProducts();
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
                loadProducts();
                updateStatistics();
            } catch (SQLException e) {
                showError("Lỗi khi xóa sản phẩm: " + e.getMessage());
            }
        }
    }

    private Dialog<Product> createProductDialog(String title, Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(product == null ? "Nhập thông tin sản phẩm" : "Sửa thông tin sản phẩm");

        ButtonType saveButtonType = new ButtonType(product == null ? "Thêm" : "Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        TextField priceField = new TextField();
        TextField quantityField = new TextField();
        TextField imagePathField = new TextField();
        Button chooseImageButton = new Button("Chọn ảnh");

        if (product != null) {
            nameField.setText(product.getName());
            priceField.setText(String.valueOf(product.getPrice()));
            quantityField.setText(String.valueOf(product.getQuantity()));
            imagePathField.setText(product.getImagePath());
        }

        grid.add(new Label("Tên sản phẩm:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Giá:"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(new Label("Số lượng:"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(new Label("Hình ảnh:"), 0, 3);
        grid.add(imagePathField, 1, 3);
        grid.add(chooseImageButton, 2, 3);

        chooseImageButton.setOnAction(_ -> handleChooseImage(imagePathField, dialog));

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText();
                    if (name.isEmpty()) {
                        showError("Tên sản phẩm không được để trống!");
                        return null;
                    }
                    
                    double price = Double.parseDouble(priceField.getText());
                    if (price <= 0) {
                        showError("Giá phải lớn hơn 0!");
                        return null;
                    }
                    
                    int quantity = Integer.parseInt(quantityField.getText());
                    if (quantity < 0) {
                        showError("Số lượng không được âm!");
                        return null;
                    }
                    
                    String imagePath = imagePathField.getText();
                    if (imagePath.isEmpty()) {
                        imagePath = "default.png";
                    }

                    return new Product(
                        product != null ? product.getId() : null,
                        name,
                        price,
                        quantity,
                        imagePath
                    );
                } catch (NumberFormatException ex) {
                    showError("Giá hoặc số lượng không hợp lệ!");
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    private void handleChooseImage(TextField imagePathField, Dialog<?> dialog) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
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
    
    @FXML
    private void handleWithdrawMoney() {
        try {
            double currentBalance = vendingMachineService.getCurrentBalance();
            
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Rút tiền");
            dialog.setHeaderText("Số dư hiện tại: " + String.format("%,.0f VNĐ", currentBalance));
            dialog.setContentText("Nhập số tiền muốn rút (VNĐ):");

            dialog.showAndWait().ifPresent(amount -> {
                try {
                    double withdrawAmount = Double.parseDouble(amount);
                    vendingMachineService.withdrawMoney(withdrawAmount);
                    
                    showNotification("Thành công", 
                        String.format("Đã rút %,.0f VNĐ khỏi máy", withdrawAmount));
                    
                    // Cập nhật hiển thị số dư mới
                    updateStatistics();
                } catch (NumberFormatException e) {
                    showError("Số tiền không hợp lệ");
                } catch (SQLException e) {
                    showError(e.getMessage());
                }
            });
        } catch (SQLException e) {
            showError("Lỗi khi lấy thông tin số dư: " + e.getMessage());
        }
    }

    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void updateStatistics() {
        totalProductsLabel.setText(String.valueOf(filteredProducts.size()));
        long lowStockCount = filteredProducts.stream().filter(product -> product.getQuantity() < 10).count();
        lowStockLabel.setText(String.valueOf(lowStockCount));
        
        double totalValue = filteredProducts.stream()
                .mapToDouble(product -> product.getPrice() * product.getQuantity())
                .sum();
        totalValueLabel.setText(String.format("%,.0f VNĐ", totalValue));

        try {
            double totalRevenue = productService.getTotalRevenue();
            totalRevenueLabel.setText(String.format("%,.0f VNĐ", totalRevenue));
            
            // Hiển thị số tiền trong máy
            double currentBalance = vendingMachineService.getCurrentBalance();
            machineBalance.setText(String.format("%,.0f VNĐ", currentBalance));
        } catch (SQLException e) {
            showError("Lỗi khi tải thông tin thống kê: " + e.getMessage());
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