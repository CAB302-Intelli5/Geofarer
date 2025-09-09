package com.example.geofarer.utils;

import com.example.geofarer.services.MapService;
import javafx.animation.FadeTransition;
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
        // Store original window position before changing scene
        double x = primaryStage.getX();
        double y = primaryStage.getY();
        boolean wasMaximized = primaryStage.isMaximized();
        // If this is the first scene, just set it
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(scene);

            // Set window position to center of screen before maximizing
            primaryStage.centerOnScreen();

            // Remove loading overlay after the window is properly sized
            Platform.runLater(() -> {
                // First maximize
                primaryStage.setMaximized(true);
                
                // Wait longer for maximization to complete
                Timeline layoutDelay = new Timeline(new KeyFrame(Duration.millis(1000), evt -> {
                    if (root instanceof StackPane) {
                        ((StackPane) root).getChildren().remove(loadingOverlay);
                    }
                    
                    // Force multiple layout passes to ensure all components are properly sized
                    finalRoot.requestLayout();
                    
                    // Additional layout pass after a small delay
                    Platform.runLater(() -> {
                        finalRoot.requestLayout();
                        
                        // Final resize simulation to trigger map sizing
                        Timeline finalDelay = new Timeline(new KeyFrame(Duration.millis(200), e -> {
                            Scene currentScene = primaryStage.getScene();
                            if (currentScene != null) {
                                // Force window to recalculate its layout
                                double width = currentScene.getWidth();
                                double height = currentScene.getHeight();
                                primaryStage.setWidth(width + 1);
                                Platform.runLater(() -> primaryStage.setWidth(width));
                            }
                        }));
                        finalDelay.play();
                    });
                }));
                layoutDelay.play();
            });
            return; // Exit early for first scene
        }

        // For subsequent scenes, use a fade transition
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), primaryStage.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            primaryStage.setScene(scene);

            // Restore window position if it wasn't maximized
            if (!wasMaximized) {
                primaryStage.setX(x);
                primaryStage.setY(y);
            }

            // Fade in the new scene
            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), scene.getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            fadeIn.setOnFinished(event -> {
                // Remove loading overlay
                if (finalRoot instanceof StackPane) {
                    ((StackPane) finalRoot).getChildren().removeIf(
                        node -> node == loadingOverlay);
                }
                
                // Force layout recalculation
                finalRoot.requestLayout();
                    // Ensure window is maximized
            // Ensure window is maximized with proper positioning
            Platform.runLater(() -> {
                // First center the window on screen
                primaryStage.centerOnScreen();
                // Then maximize it
                if (primaryStage != null) {
                    primaryStage.setMaximized(true);
                    
                    // Wait for maximization to complete before triggering layout
                    Timeline maxDelay = new Timeline(new KeyFrame(Duration.millis(300), evt -> {
                        finalRoot.requestLayout();
                        
                        // Simulate a resize event to ensure all components update
                        Scene currentScene = primaryStage.getScene();
                        if (currentScene != null) {
                            double width = currentScene.getWidth();
                            double height = currentScene.getHeight();
                            primaryStage.setWidth(width + 1);
                            primaryStage.setWidth(width);
                        }
                    }));
                    maxDelay.play();
                }
            });

            fadeIn.play();
            });

        fadeOut.play();
        });
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
