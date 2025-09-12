package com.example.geofarer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/geofarer/MainPage.fxml"));
        BorderPane root = fxmlLoader.load();
        Scene scene = new Scene(root, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("GeoFarer");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
