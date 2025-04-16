module com.vendingmachine {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive java.sql;
    requires java.base;
    
    opens com.vendingmachine to javafx.fxml;
    opens com.vendingmachine.controller to javafx.fxml;
    opens com.vendingmachine.database to java.sql;
    opens com.vendingmachine.model to javafx.base;
    
    exports com.vendingmachine;
    exports com.vendingmachine.controller;
    exports com.vendingmachine.database;
    exports com.vendingmachine.model;
}