package com.example.geofarer;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class BaseController {

    @FXML protected StackPane userCirclePane;
    @FXML protected CheckBox showPasswordCheckBox;
    @FXML protected PasswordField passwordField;
    @FXML protected TextField passwordVisibleField;
    @FXML protected TextField emailField;

    protected ContextMenu userMenu;

    @FXML
    public void initialize() {
        setupUserMenu();
    }

    // Dropdown menu options
    private void setupUserMenu() {
        userMenu = new ContextMenu();

        MenuItem profile = new MenuItem("Profile");
        MenuItem login = new MenuItem("Login");
        login.setOnAction(e -> openLoginPage());
        MenuItem signup = new MenuItem("Sign Up");
        signup.setOnAction(e -> openSignUpPage());
        MenuItem settings = new MenuItem("Settings");
        MenuItem logout = new MenuItem("Log Out");

        userMenu.getItems().addAll(profile, login, signup, settings, logout);
    }

    // Function for when the user icon in the top right is clicked
    @FXML
    protected void onUserCircleClick(MouseEvent event) {
        if (userMenu.isShowing()) {
            userMenu.hide();
        } else {
            userMenu.show(userCirclePane, event.getScreenX(), event.getScreenY());
        }
    }

    // Function for toggling the hidden password on and off
    @FXML
    protected void onTogglePassword() {
        if (showPasswordCheckBox.isSelected()) {
            passwordVisibleField.setText(passwordField.getText());
            passwordVisibleField.setVisible(true);
            passwordField.setVisible(false);
        } else {
            passwordField.setText(passwordVisibleField.getText());
            passwordField.setVisible(true);
            passwordVisibleField.setVisible(false);
        }
    }

    // Open Login Page
    protected void openLoginPage() {
        Stage stage = (Stage) userCirclePane.getScene().getWindow();
        PageLoader.openPage("/com/example/geofarer/LoginPage.fxml", "Login", stage);
    }

    // Open Sign Up Page
    protected void openSignUpPage() {
        Stage stage = (Stage) userCirclePane.getScene().getWindow();
        PageLoader.openPage("/com/example/geofarer/SignUpPage.fxml", "Sign Up", stage);
    }
}
