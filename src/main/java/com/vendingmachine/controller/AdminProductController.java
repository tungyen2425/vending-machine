package com.vendingmachine.controller;

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
    
    private ObservableList<Product> products = FXCollections.observableArrayList();
    private FilteredList<Product> filteredProducts;
    
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
    }
    
    private void loadProductsFromDatabase() {
        String sql = "SELECT * FROM products ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Product product = new Product(
                    String.valueOf(rs.getInt("id")),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getInt("quantity"),
                    rs.getString("image_url")
                );
                products.add(product);
            }
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
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Thêm sản phẩm mới");
        dialog.setHeaderText("Nhập thông tin sản phẩm");

        ButtonType addButtonType = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        TextField priceField = new TextField();
        TextField quantityField = new TextField();
        TextField imagePathField = new TextField();
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

        chooseImageButton.setOnAction(e -> {
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
        });

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String name = nameField.getText();
                    if (name.isEmpty()) {
                        showError("Tên sản phẩm không được để trống!");
                        return null;
                    }
                    
                    double price;
                    try {
                        price = Double.parseDouble(priceField.getText());
                        if (price <= 0) {
                            showError("Giá phải lớn hơn 0!");
                            return null;
                        }
                    } catch (NumberFormatException ex) {
                        showError("Giá không hợp lệ!");
                        return null;
                    }
                    
                    int quantity;
                    try {
                        quantity = Integer.parseInt(quantityField.getText());
                        if (quantity < 0) {
                            showError("Số lượng không được âm!");
                            return null;
                        }
                    } catch (NumberFormatException ex) {
                        showError("Số lượng không hợp lệ!");
                        return null;
                    }
                    
                    String imagePath = imagePathField.getText();
                    if (imagePath.isEmpty()) {
                        imagePath = "default.png";
                    }

                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(
                             "INSERT INTO products (name, price, quantity, image_url) VALUES (?, ?, ?, ?)",
                             Statement.RETURN_GENERATED_KEYS)) {
                        
                        stmt.setString(1, name);
                        stmt.setDouble(2, price);
                        stmt.setInt(3, quantity);
                        stmt.setString(4, imagePath);
                        
                        int affectedRows = stmt.executeUpdate();
                        
                        if (affectedRows > 0) {
                            ResultSet rs = stmt.getGeneratedKeys();
                            if (rs.next()) {
                                int id = rs.getInt(1);
                                Product newProduct = new Product(String.valueOf(id), name, price, quantity, imagePath);
                                products.add(newProduct);
                                updateStatistics();
                                return newProduct;
                            }
                        }
                    } catch (SQLException ex) {
                        showError("Lỗi khi thêm sản phẩm vào database: " + ex.getMessage());
                    }
                } catch (Exception ex) {
                    showError("Lỗi: " + ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void handleEditProduct(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Sửa sản phẩm");
        dialog.setHeaderText("Sửa thông tin sản phẩm");

        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField(product.getName());
        TextField priceField = new TextField(String.valueOf(product.getPrice()));
        TextField quantityField = new TextField(String.valueOf(product.getQuantity()));
        TextField imagePathField = new TextField(product.getImagePath());
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

        chooseImageButton.setOnAction(e -> {
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
        });

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText();
                    if (name.isEmpty()) {
                        showError("Tên sản phẩm không được để trống!");
                        return null;
                    }
                    
                    double price;
                    try {
                        price = Double.parseDouble(priceField.getText());
                        if (price <= 0) {
                            showError("Giá phải lớn hơn 0!");
                            return null;
                        }
                    } catch (NumberFormatException ex) {
                        showError("Giá không hợp lệ!");
                        return null;
                    }
                    
                    int quantity;
                    try {
                        quantity = Integer.parseInt(quantityField.getText());
                        if (quantity < 0) {
                            showError("Số lượng không được âm!");
                            return null;
                        }
                    } catch (NumberFormatException ex) {
                        showError("Số lượng không hợp lệ!");
                        return null;
                    }
                    
                    String imagePath = imagePathField.getText();
                    if (imagePath.isEmpty()) {
                        imagePath = product.getImagePath();
                    }

                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(
                             "UPDATE products SET name = ?, price = ?, quantity = ?, image_url = ? WHERE id = ?")) {
                        
                        stmt.setString(1, name);
                        stmt.setDouble(2, price);
                        stmt.setInt(3, quantity);
                        stmt.setString(4, imagePath);
                        stmt.setInt(5, Integer.parseInt(product.getId()));
                        
                        int affectedRows = stmt.executeUpdate();
                        
                        if (affectedRows > 0) {
                            product.setName(name);
                            product.setPrice(price);
                            product.setQuantity(quantity);
                            product.setImagePath(imagePath);
                            productTable.refresh();
                            updateStatistics();
                            return product;
                        }
                    } catch (SQLException ex) {
                        showError("Lỗi khi cập nhật sản phẩm: " + ex.getMessage());
                    }
                } catch (Exception ex) {
                    showError("Lỗi: " + ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void handleDeleteProduct(Product product) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận xóa");
        confirmDialog.setHeaderText("Xóa sản phẩm");
        confirmDialog.setContentText("Bạn có chắc muốn xóa sản phẩm này?");

        if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM products WHERE id = ?")) {
                
                stmt.setInt(1, Integer.parseInt(product.getId()));
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    products.remove(product);
                    updateStatistics();
                }
            } catch (SQLException ex) {
                showError("Lỗi khi xóa sản phẩm: " + ex.getMessage());
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static class Product {
        private String id;
        private String name;
        private double price;
        private int quantity;
        private String imagePath;
        
        public Product(String id, String name, double price, int quantity, String imagePath) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.imagePath = imagePath;
        }
        
     
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getImagePath() { return imagePath; }
        public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    }
}