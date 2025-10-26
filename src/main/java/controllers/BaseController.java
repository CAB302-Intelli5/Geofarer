package controllers;

import javafx.geometry.Side;
import javafx.scene.control.*;
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
    @FXML
    protected Button settingsButton;

    protected ContextMenu userMenu;

    /**
     * Initializes of the controller designed for future initializations if needed
     */
    @FXML
    public void initialize() {
        setupUserMenu();
    }


    protected void setupUserMenu() {
        userMenu = new ContextMenu();

    MenuItem profile = new MenuItem("Profile");
    MenuItem settings = new MenuItem("Settings");
    // Open settings when clicked
    settings.setOnAction(e -> openSettingsPage());
    MenuItem logout = new MenuItem("Log Out");
        // Profile navigates to user stats view
        profile.setOnAction(e -> {
            if (loginButton != null && loginButton.getScene() != null) {
                Stage stage = (Stage) loginButton.getScene().getWindow();
                if (stage != null) PageLoader.openUserStatsView("My Stats - Geofarer", stage);
            }
        });

        // (no-op) settings action already set above to open settings page

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
     * Handles a click on the user icon and toggles the visibility of the user dropdown
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

    /**
     * Opens the login page
     */
    protected void openLoginPage() {
        Stage stage = (Stage) userCirclePane.getScene().getWindow();
        PageLoader.openPage("/pages/LoginPage.fxml", "Login", stage);
    }

    /**
     * Handles the sign up page in current stage
     */
    protected void openSignUpPage() {
        Stage stage = (Stage) userCirclePane.getScene().getWindow();
        PageLoader.openPage("/pages/SignUp.fxml", "Sign Up", stage);
    }

    /**
     * Opens the Settings page
     */
    protected void openSettingsPage() {
        // Try to resolve a stage from available injected nodes in a safe order
        Stage stage = null;
        if (settingsButton != null && settingsButton.getScene() != null) {
            stage = (Stage) settingsButton.getScene().getWindow();
        } else if (loginButton != null && loginButton.getScene() != null) {
            stage = (Stage) loginButton.getScene().getWindow();
        } else if (userCirclePane != null && userCirclePane.getScene() != null) {
            stage = (Stage) userCirclePane.getScene().getWindow();
        }

        if (stage != null) {
            PageLoader.openPage("/pages/SettingsPage.fxml", "Settings", stage);
        } else {
            System.err.println("BaseController: could not find a Stage to open Settings page");
        }
    }
}