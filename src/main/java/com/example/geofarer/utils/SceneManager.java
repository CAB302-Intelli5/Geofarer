package com.example.geofarer.utils;

import com.example.geofarer.services.MapService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;


public class SceneManager {
    private static Stage primaryStage;
    private static MapService mapService;
    public static void initialise(Stage stage) {
        primaryStage = stage;
        mapService = new MapService();
    }
    public static void switchToScene(Pane root) {
        System.out.println("SceneManager: switchToScene called");
        
        if (primaryStage == null) {
            System.err.println("SceneManager: primaryStage is null!");
            return;
        }
        
        try {
            // Create loading overlay
            ProgressIndicator progress = new ProgressIndicator();
            Label loadingLabel = new Label("Loading...");
            loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            StackPane loadingContent = new StackPane();
            loadingContent.getChildren().addAll(progress, loadingLabel);
            loadingContent.setStyle("-fx-spacing: 10;");

            StackPane loadingOverlay = new StackPane(loadingContent);
            loadingOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");

            // Add loading overlay to the new scene's root
            final Pane finalRoot;
            if (root instanceof StackPane) {
                // Apply critical style to ensure full window coverage
                root.setStyle(root.getStyle() + "; -fx-background-color: white;");
                ((StackPane) root).getChildren().add(loadingOverlay);
                finalRoot = root;
            } else {
                StackPane wrapperPane = new StackPane(root, loadingOverlay);
                // Apply critical style to ensure full window coverage
                wrapperPane.setStyle("-fx-background-color: white;");
                finalRoot = wrapperPane;
            }
            
            // Make sure content fills available space
            finalRoot.setPrefWidth(Double.MAX_VALUE);
            finalRoot.setPrefHeight(Double.MAX_VALUE);
            finalRoot.setMaxWidth(Double.MAX_VALUE);
            finalRoot.setMaxHeight(Double.MAX_VALUE);
            
            Scene scene = new Scene(finalRoot);
            System.out.println("SceneManager: New scene created");
            
            // If this is the first scene, just set it
            if (primaryStage.getScene() == null) {
                System.out.println("SceneManager: Setting first scene");
                primaryStage.setScene(scene);
                primaryStage.centerOnScreen();

                // Remove loading overlay after the window is properly sized
                Platform.runLater(() -> {
                    primaryStage.setMaximized(true);
                    
                    Timeline layoutDelay = new Timeline(new KeyFrame(Duration.millis(1000), evt -> {
                        if (finalRoot instanceof StackPane) {
                            ((StackPane) finalRoot).getChildren().remove(loadingOverlay);
                        }
                        finalRoot.requestLayout();
                    }));
                    layoutDelay.play();
                });
                return;
            }

            // For subsequent scenes, do immediate switch with debug logging
            System.out.println("SceneManager: Switching to subsequent scene");
            
            // Set the new scene immediately
            primaryStage.setScene(scene);
            System.out.println("SceneManager: Scene set on primaryStage");
            
            // Ensure the stage is showing
            if (!primaryStage.isShowing()) {
                primaryStage.show();
                System.out.println("SceneManager: Stage shown");
            }
            
            // Remove loading overlay after a short delay
            Platform.runLater(() -> {
                Timeline removeOverlayDelay = new Timeline(new KeyFrame(Duration.millis(500), evt -> {
                    if (finalRoot instanceof StackPane) {
                        ((StackPane) finalRoot).getChildren().remove(loadingOverlay);
                    }
                    finalRoot.requestLayout();
                    System.out.println("SceneManager: Loading overlay removed and layout requested");
                }));
                removeOverlayDelay.play();
            });
            
        } catch (Exception e) {
            System.err.println("SceneManager: Exception during scene switch: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static void preloadResources() {
        Task<Void> preloadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Preload map image
                mapService.loadRasterImage();

                // Preload shapefile data
                mapService.loadShapefileData();

                return null;
            }
        };

        new Thread(preloadTask).start();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
