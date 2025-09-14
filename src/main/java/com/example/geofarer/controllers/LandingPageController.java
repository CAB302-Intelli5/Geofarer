package com.example.geofarer.controllers;

import com.example.geofarer.utils.PageLoader;
import com.example.geofarer.utils.SceneManager;
import com.example.geofarer.views.GameView;
import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LandingPageController {

    private Button loginButton;

    // Method for the View to pass the button reference
    public void setLoginButton(Button loginButton) {
        this.loginButton = loginButton;
    }

    /**
     * Handles the click event for the login button.
     * Navigates the user to the login page.
     */

    public void onLoginButtonClick() {
        if (loginButton != null && loginButton.getScene() != null && loginButton.getScene().getWindow() != null) {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            PageLoader.openPage("/pages/LoginPage.fxml", "Login", stage);
        } else {
            System.err.println("Could not get the stage from the login button.");
        }
    }

    public void startGame() {
        System.out.println("Play button clicked, initializing game view");
        GameView gameView = new GameView(true);
        SceneManager.switchToScene(gameView);

        PauseTransition delay = new PauseTransition(Duration.millis(100)); // Shortened delay
        delay.setOnFinished(e -> {
            System.out.println("Initializing map");
            gameView.initializeMap();
        });
        delay.play();
    }

    public void showLogin() { }
    public void showRegister() { }
    public void showStats() { }
}