package com.example.geofarer.controllers;

import com.example.geofarer.UserService;
import com.example.geofarer.utils.SceneManager;
import com.example.geofarer.views.GameView;
import com.example.geofarer.views.LandingPageView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.geofarer.utils.PageLoader;
import javafx.scene.control.Button;

import java.awt.*;
import java.io.IOException;

public class LoginController extends BaseController {

    @FXML
    private Button goBackButton;
    private Button loginButton;

    private UserService userService = new UserService();

    @FXML
    private void onLoginClick() { // When the Login button is clicked
        String email = emailField.getText();
        String password;
        if (passwordVisibleField.isVisible()) {
            password = passwordVisibleField.getText();
        } else {
            password = passwordField.getText();
        }
        if(userService.validateLogin(email, password)) {
            System.out.println("Login successful: " + email);

            GameView gameView = new GameView(true);
            SceneManager.switchToScene(gameView);
        } else {
            System.out.println("Invalid email or password");
        }
    }

    @FXML
    private void onGoBackClick() { // When the Go Back button is clicked
        LandingPageView landingView = new LandingPageView();
        SceneManager.switchToScene(landingView);

        Stage stage = SceneManager.getPrimaryStage();
        stage.setTitle("Geofarer - Geography Learning Game");
    }

    /**
     * Navigates to the sign-up page when the link is clicked.
     */
    @FXML
    private void onSignUpLinkClick() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        // Use the PageLoader to open the SignUpPage
        PageLoader.openPage("/pages/SignUp.fxml", "Sign Up", stage);
    }
}