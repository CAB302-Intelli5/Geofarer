package com.example.geofarer.views;

import com.example.geofarer.controllers.LandingPageController;
import com.example.geofarer.services.MapService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LandingPageView extends StackPane {
    private LandingPageController controller;
    private MapService mapService;

    public LandingPageView() {
        this.mapService = new MapService();
        this.controller = new LandingPageController();

        // Set size constraints to prevent oversizing
        this.setMaxWidth(Double.MAX_VALUE);
        this.setMaxHeight(Double.MAX_VALUE);
        this.setPrefWidth(Region.USE_COMPUTED_SIZE);
        this.setPrefHeight(Region.USE_COMPUTED_SIZE);

        initialiseView();
    }

    private void initialiseView() {
        //Background map over the full screen
        ImageView backgroundMap = new ImageView();
        backgroundMap.setPreserveRatio(false);
        backgroundMap.fitHeightProperty().bind(this.heightProperty());
        backgroundMap.fitWidthProperty().bind(this.widthProperty());

        //Load and set the map image
        backgroundMap.setImage(mapService.loadRasterImage());

        //Semi-transparent overlay with responsive padding
        VBox overlay = new VBox(20);
        overlay.setAlignment(Pos.CENTER);
        
        // Use percentage-based padding instead of fixed padding
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        
        // Bind padding to window size to make it responsive
        overlay.paddingProperty().bind(javafx.beans.binding.Bindings.createObjectBinding(() -> {
            double width = this.getWidth();
            double height = this.getHeight();
            double padding = Math.min(width * 0.05, height * 0.05); // 5% of smaller dimension
            return new javafx.geometry.Insets(Math.max(20, padding)); // Minimum 20px padding
        }, this.widthProperty(), this.heightProperty()));

        //Welcome text
        Label welcomeLabel = new Label("Welcome to Geofarer");
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 48));
        welcomeLabel.setTextFill(Color.WHITE);

        //Play button
        Button playButton = new Button("PLAY NOW");
        playButton.setFont(Font.font("System", FontWeight.BOLD, 24));

        //BUtton  hober effects

        //On button click start the game
        playButton.setOnAction(e -> controller.startGame());

        overlay.getChildren().addAll(welcomeLabel, playButton);
        this.getChildren().addAll(backgroundMap, overlay);
    }
}
