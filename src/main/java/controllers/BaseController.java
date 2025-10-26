package controllers;

import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.geometry.Side;
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

    @FXML
    protected StackPane userCirclePane;
    @FXML
    protected CheckBox showPasswordCheckBox;
    @FXML
    protected PasswordField passwordField;
    @FXML
    protected TextField passwordVisibleField;
    @FXML
    protected TextField emailField;
    @FXML
    protected Button loginButton;

    protected ContextMenu userMenu;

    /**
     * Initializes of the controller designed for future initializations if needed
     */
    @FXML
    public void initialize() {
        setupUserMenu();
    }

    // Dropdown menu options
    public void setupUserMenu() {
        userMenu = new ContextMenu();

        MenuItem profile = new MenuItem("Profile");
        MenuItem settings = new MenuItem("Settings");
        MenuItem logout = new MenuItem("Log Out");
        // Profile navigates to user stats view
        profile.setOnAction(e -> {
            if (loginButton != null && loginButton.getScene() != null) {
                Stage stage = (Stage) loginButton.getScene().getWindow();
                if (stage != null) PageLoader.openUserStatsView("My Stats - Geofarer", stage);
            }
        });

        // Settings - placeholder for future
        settings.setOnAction(e -> {
            System.out.println("Settings clicked - not implemented");
        });

        // Logout - clear session and return to landing page
        logout.setOnAction(e -> {
            utils.SessionManager.getInstance().logout();
            if (loginButton != null && loginButton.getScene() != null) {
                Stage stage = (Stage) loginButton.getScene().getWindow();
                if (stage != null) PageLoader.openPage("/pages/LandingPage.fxml", "Geofarer - Geography Learning Game", stage);
            }
        });

        userMenu.getItems().addAll(profile, settings, logout);
    }

    /**
     * Handles a click on the user icon and togges the visibility of the user dropdown
     * @param event the mouse triggered by clicking the user icon
     */
    @FXML
    protected void onUserCircleClick(MouseEvent event) {
        // Only show the dropdown menu if a user is logged in.
        if (utils.SessionManager.getInstance().isLoggedIn()) {
            if (userMenu.isShowing()) {
                userMenu.hide();
            } else {
                userMenu.show(userCirclePane, Side.BOTTOM, 0, 0);
            }
        } else {
            // Not logged in -> navigate to login page
            openLoginPage();
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
        PageLoader.openPage("@../pages/LoginPage.fxml", "Login", stage);
    }

    /**
     * Handles the sign up page in current stage
     */
    protected void openSignUpPage() {
        Stage stage = (Stage) userCirclePane.getScene().getWindow();
        PageLoader.openPage("@../pages/SignUpPage.fxml", "Sign Up", stage);
    }
}