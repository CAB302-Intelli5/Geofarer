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
        
        // Wait for the scene to be properly laid out before initializing map
        System.out.println("Setting up map initialization delay");
        PauseTransition delay = new PauseTransition(Duration.millis(2000)); // Increased delay
        delay.setOnFinished(e -> {
            System.out.println("Initializing map");
            // Force layout update before map initialization
            gameView.applyCss();
            gameView.autosize();
            gameView.requestLayout();
            
            // Additional delay to ensure layout is complete
            PauseTransition layoutDelay = new PauseTransition(Duration.millis(500));
            layoutDelay.setOnFinished(event -> {
                System.out.println("Layout complete, starting map initialization");
                gameView.initializeMap();
            });
            layoutDelay.play();
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
