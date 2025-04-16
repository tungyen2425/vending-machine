package com.vendingmachine;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("fxml/mainpage.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 680); 
        scene.getStylesheets().add(getClass().getResource("css/mainpage.css").toExternalForm());
        stage.setTitle("Vending Machine");
        stage.setScene(scene);
        stage.setResizable(false);  
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}