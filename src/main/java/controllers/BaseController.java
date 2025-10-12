package controllers;

import javafx.geometry.Side;
import javafx.scene.control.*;
import utils.PageLoader;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

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

        userMenu.getItems().addAll(profile, settings, logout);
    }

    // Function for when the user icon in the top right is clicked
    @FXML
    protected void onUserCircleClick(MouseEvent event) {
        if (userMenu.isShowing()) {
            userMenu.hide();
        } else {
            userMenu.show(userCirclePane, Side.BOTTOM, 0, 0);
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
        PageLoader.openPage("@../pages/LoginPage.fxml", "Login", stage);
    }

    // Open Sign Up Page
    protected void openSignUpPage() {
        Stage stage = (Stage) userCirclePane.getScene().getWindow();
        PageLoader.openPage("@../pages/SignUpPage.fxml", "Sign Up", stage);
    }
}