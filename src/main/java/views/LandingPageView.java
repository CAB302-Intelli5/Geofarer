package views;

import controllers.LandingPageController;
import javafx.scene.AccessibleRole;
import javafx.scene.text.Text;
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
        //with functional screen reader set up
        VBox overlay = new VBox(20);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        Text welcomeText1 = new Text("WELCOME TO ");
        welcomeText1.setFont(Font.font("Inika", FontWeight.NORMAL, 48));
        welcomeText1.setFill(Color.WHITE);

        Text welcomeText2 = new Text("GEOFARER");
        welcomeText2.setFont(Font.font("Inika", FontWeight.BOLD, 68));
        welcomeText2.setFill(Color.WHITE);

        VBox welcomeTextBox = new VBox(5, welcomeText1, welcomeText2);
        welcomeTextBox.setAlignment(Pos.CENTER);

        Label welcomeLabel = new Label();
        welcomeLabel.setGraphic(welcomeTextBox);

        //for pronunciation purposes, spelled as such
        welcomeLabel.setAccessibleText("welcome to geo farer");
        welcomeLabel.setFocusTraversable(true);

        Button playButton = new Button("PLAY NOW");
        playButton.setFont(Font.font("TASA Explorer", FontWeight.BOLD, 24));
        playButton.setAccessibleText("Click to start the game");
        playButton.setFocusTraversable(true);

        //On button click start the game
        playButton.setOnAction(e -> controller.startGame());

        overlay.getChildren().addAll(welcomeLabel, playButton);

        // 3. Login Button (Top Right)
        Button loginButton = new Button("Login");
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