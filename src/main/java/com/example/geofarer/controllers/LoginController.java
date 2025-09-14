package com.example.geofarer.controllers;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import com.example.geofarer.utils.PageLoader;

public class LoginController extends BaseController {


    @FXML
    private void onLoginClick() { // When the Login button is clicked
        String email = emailField.getText();
        String password = passwordField.getText();
        System.out.println("Logging in with: Email: " + email + ", Password: " + password);
    }

    @FXML
    private void onGoBackClick() { // When the Go Back button is clicked

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