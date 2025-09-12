package com.example.geofarer;

import javafx.fxml.FXML;

public class SignUpController extends BaseController {

    @FXML
    private void onSignUpClick() { // When the Sign Up button is clicked
        String email = emailField.getText();
        String password = passwordField.getText();
        System.out.println("Signing up with: Email: " + email + ", Password: " + password);
    }

    @FXML
    private void onGoBackClick() { // When the Go Back button is clicked

    }
}
