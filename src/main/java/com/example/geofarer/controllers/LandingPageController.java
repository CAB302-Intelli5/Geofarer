package com.example.geofarer.controllers;


import com.example.geofarer.utils.SceneManager;
import com.example.geofarer.views.GameView;
import javafx.util.Duration;
import javafx.animation.PauseTransition;

public class LandingPageController {
    public void startGame() {
        // Create game view with delayed initialization
        GameView gameView = new GameView(true); // Use the constructor with the delay parameter
        
        // Switch to the scene first (only call this once)
        SceneManager.switchToScene(gameView);
        
        // Then trigger map initialization after scene is fully rendered
        PauseTransition delay = new PauseTransition(Duration.millis(1000));
        delay.setOnFinished(e -> gameView.initializeMap());
        delay.play();
    }
    public void showLogin() {

    }
    public void showRegister() {

    }

    public void showStats(){

    }
}
