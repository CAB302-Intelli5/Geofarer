package com.example.geofarer;

import javafx.fxml.FXML;

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
}
