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

        // Set initial size with some buffer for Windows decorations
        primaryStage.setWidth(1024);
        primaryStage.setHeight(768);

        // Switch to landing page
        SceneManager.switchToScene(new LandingPageView());

        primaryStage.show();
        
        // Force proper layout calculation on Windows after showing
        Platform.runLater(() -> {
            // More aggressive layout forcing for Windows
            if (primaryStage.getScene() != null && primaryStage.getScene().getRoot() != null) {
                // Force multiple layout passes
                for (int i = 0; i < 3; i++) {
                    primaryStage.getScene().getRoot().applyCss();
                    primaryStage.getScene().getRoot().autosize();
                    primaryStage.getScene().getRoot().requestLayout();
                }
                
                // Force a window resize to trigger proper container sizing
                double currentWidth = primaryStage.getWidth();
                double currentHeight = primaryStage.getHeight();
                
                // More dramatic resize to force layout
                primaryStage.setWidth(currentWidth + 10);
                primaryStage.setHeight(currentHeight + 10);
                
                Platform.runLater(() -> {
                    primaryStage.setWidth(currentWidth);
                    primaryStage.setHeight(currentHeight);
                    
                    // Force another layout pass after resize with delay
                    Platform.runLater(() -> {
                        // Multiple layout passes again
                        for (int i = 0; i < 3; i++) {
                            primaryStage.getScene().getRoot().applyCss();
                            primaryStage.getScene().getRoot().autosize();
                            primaryStage.getScene().getRoot().requestLayout();
                        }
                        
                        // Final positioning
                        Platform.runLater(() -> {
                            primaryStage.centerOnScreen();
                            // One final layout pass
                            primaryStage.getScene().getRoot().requestLayout();
                        });
                    });
                });
            }
        });

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

        // Preload resources in background
        SceneManager.preloadResources();
    }

    public static void main(String[] args) {
        launch(args);
    }
}