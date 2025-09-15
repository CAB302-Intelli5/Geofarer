package com.example.geofarer.controllers;

import com.example.geofarer.utils.PageLoader;
import com.example.geofarer.utils.SceneManager;
import com.example.geofarer.views.LandingPageView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class SignUpController extends BaseController {

    @FXML
    private Button goBackButton;

    @FXML
    private void onSignUpClick() { // When the Sign Up button is clicked
        String email = emailField.getText();
        String password = passwordField.getText();
        System.out.println("Signing up with: Email: " + email + ", Password: " + password);
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