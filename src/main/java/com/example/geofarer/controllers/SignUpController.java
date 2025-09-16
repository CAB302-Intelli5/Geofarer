package com.example.geofarer.controllers;

import com.example.geofarer.UserService;
import com.example.geofarer.utils.PageLoader;
import com.example.geofarer.utils.SceneManager;
import com.example.geofarer.views.GameView;
import com.example.geofarer.views.LandingPageView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class SignUpController extends BaseController {

    @FXML
    private Button goBackButton;
    private Button signupButton;

    private UserService userService = new UserService();

    @FXML
    private void onSignUpClick() { // When the Sign Up button is clicked
        String email = emailField.getText();
        String password = passwordField.getText();
        if(userService.addUser(email, password)) {
            System.out.println("User added successfully: " + email);

            GameView gameView = new GameView(true);
            SceneManager.switchToScene(gameView);
        } else {
            System.out.println("Failed to add user");
        }
    }

    @FXML
    private void onGoBackClick() { // When the Go Back button is clicked
        LandingPageView landingView = new LandingPageView();
        SceneManager.switchToScene(landingView);

        Stage stage = SceneManager.getPrimaryStage();
        stage.setTitle("Geofarer - Geography Learning Game");
    }

    @FXML
    private void onLoginLinkClick() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        // Use the PageLoader to open the SignUpPage
        PageLoader.openPage("/pages/LoginPage.fxml", "Login", stage);
    }
}