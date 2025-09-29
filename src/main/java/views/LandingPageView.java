package views;

import controllers.LandingPageController;
import model.MapService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LandingPageView extends StackPane {

    private final LandingPageController controller;
    private final MapService mapService;

    public LandingPageView() {
        this.mapService = new MapService();
        this.controller = new LandingPageController();

        // Set size constraints
        this.setMaxWidth(Double.MAX_VALUE);
        this.setMaxHeight(Double.MAX_VALUE);

        initialiseView();
    }

    private void initialiseView() {
        // 1. Background Map
        ImageView backgroundMap = new ImageView(mapService.loadRasterImage());
        backgroundMap.setPreserveRatio(false);
        backgroundMap.fitHeightProperty().bind(this.heightProperty());
        backgroundMap.fitWidthProperty().bind(this.widthProperty());

        // 2. Center Overlay
        VBox overlay = new VBox(20);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        Label welcomeLabel1 = new Label("Welcome to");
        welcomeLabel1.setFont(Font.font("Inika", 48));
        welcomeLabel1.setTextFill(Color.WHITE);

        Label welcomeLabel2 = new Label("Geofarer");
        welcomeLabel2.setFont(Font.font("Inika", FontWeight.BOLD, 52));
        welcomeLabel2.setTextFill(Color.WHITE);


        Button playButton = new Button("PLAY NOW");
        playButton.setFont(Font.font("System", FontWeight.BOLD, 24));

        //On button click start the game
        playButton.setOnAction(e -> controller.startGame());

        overlay.getChildren().addAll(welcomeLabel1, welcomeLabel2, playButton);

        // 3. Login Button (Top Right)
        Button loginButton = new Button();
        try {
            // Load the icon image. This path is critical.
            Image loginIcon = new Image(getClass().getResourceAsStream("/images/login_icon.png"));
            ImageView iconView = new ImageView(loginIcon);
            iconView.setFitWidth(40);
            iconView.setFitHeight(40);
            iconView.setPreserveRatio(true);
            loginButton.setGraphic(iconView);
        } catch (Exception e) {
            System.err.println("Error loading login icon: " + e.getMessage());
            // Fallback to text if the icon fails to load
            loginButton.setText("Login");
        }

        loginButton.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        loginButton.setOnAction(e -> controller.onLoginButtonClick());

        // Pass the button to the controller so it can access the scene/stage
        controller.setLoginButton(loginButton);

        // Add all components to the StackPane
        this.getChildren().addAll(backgroundMap, overlay, loginButton);

        // 4. Position the Login Button in the Top-Right
        StackPane.setAlignment(loginButton, Pos.TOP_RIGHT);
        StackPane.setMargin(loginButton, new Insets(10, 10, 0, 0));
    }
}