package com.example.geofarer;

import com.example.geofarer.utils.SceneManager;
import com.example.geofarer.views.LandingPageView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class GeofarerApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        //Setup an app icon

        // Initialize scene manager
        SceneManager.initialise(primaryStage);

        // Set window constraints and properties
        primaryStage.setTitle("Geofarer - Geography Learning Game");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        primaryStage.setWidth(1024);
        primaryStage.setHeight(768);

        // listener for maximized property changes and screen size
        primaryStage.maximizedProperty().addListener((obs, wasMaximized, isMaximized) -> {
            if (primaryStage.getScene() != null && primaryStage.getScene().getRoot() != null) {
                // If unmaximizing, make sure window is visible
                if (!isMaximized && wasMaximized) {
                    // Center window after unmaximizing
                    Platform.runLater(() -> {
                        primaryStage.centerOnScreen();
                        primaryStage.getScene().getRoot().requestLayout();
                    });
                } else {
                    primaryStage.getScene().getRoot().requestLayout();
                }
            }
        });

        // Switch to landing page
        SceneManager.switchToScene(new LandingPageView());


        primaryStage.show();
        Platform.runLater(() -> primaryStage.setMaximized(true));

        // Preload resources in background
        SceneManager.preloadResources();
    }

    public static void main(String[] args) {
        launch(args);
    }
}