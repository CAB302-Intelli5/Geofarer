package controllers;

import utils.PageLoader;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Shared controller class used for common UI functionality,
 * this includes login/signup and navigation
 */
public class BaseController {

    @FXML protected StackPane userCirclePane;
    @FXML protected CheckBox showPasswordCheckBox;
    @FXML protected PasswordField passwordField;
    @FXML protected TextField passwordVisibleField;
    @FXML protected TextField emailField;

    protected ContextMenu userMenu;

    /**
     * Initializes of the controller designed for future initializations if needed
     */
    @FXML
    public void initialize() {
        setupUserMenu();
    }


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

    /**
     * Handles a click on the user icon and togges the visibility of the user dropdown
     * @param event the mouse triggered by clicking the user icon
     */
    @FXML
    protected void onUserCircleClick(MouseEvent event) {
        if (userMenu.isShowing()) {
            userMenu.hide();
        } else {
            userMenu.show(userCirclePane, event.getScreenX(), event.getScreenY());
        }
    }

    /**
     * Toggles the visibility of the password field
     */
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

    /**
     * Opens the login page
     */
    protected void openLoginPage() {
        Stage stage = (Stage) userCirclePane.getScene().getWindow();
        PageLoader.openPage("/com/example/geofarer/pages/LoginPage.fxml", "Login", stage);
    }

    /**
     * Handles the sign up page in current stage
     */
    protected void openSignUpPage() {
        Stage stage = (Stage) userCirclePane.getScene().getWindow();
        PageLoader.openPage("/com/example/geofarer/pages/SignUpPage.fxml", "Sign Up", stage);
    }
}