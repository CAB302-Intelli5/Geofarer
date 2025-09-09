package com.example.geofarer.controllers;


import com.example.geofarer.utils.SceneManager;
import com.example.geofarer.views.GameView;
import javafx.util.Duration;
import javafx.animation.PauseTransition;

public class LandingPageController {
    public void startGame() {
        System.out.println("Play button clicked, initializing game view");
        // Create game view with delayed initialization
        GameView gameView = new GameView(true); // Use the constructor with the delay parameter
        
        // Switch to the scene first (only call this once)
        System.out.println("Switching to game scene");
        SceneManager.switchToScene(gameView);
        
        // Then trigger map initialization after scene is fully rendered
        System.out.println("Setting up map initialization delay");
        PauseTransition delay = new PauseTransition(Duration.millis(1000));
            delay.setOnFinished(e -> {
        System.out.println("Initializing map");
        gameView.initializeMap();
    });
        delay.play();
    }
    public void showLogin() {

    }
    public void showRegister() {

    }

    public void showStats(){

    }
}
