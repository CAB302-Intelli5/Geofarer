package controllers;

import model.UserService;
import utils.SceneManager;
import views.GameView;
import views.LandingPageView;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import utils.PageLoader;
import javafx.scene.control.Button;

/**
 * Controller for the login page
 * Handles the user login, navigation back to the landing page, and nav to the sign-up page
 * Inherits from {@link BaseController} to access common UI elements like email and password fields.
 */
public class LoginController extends BaseController {

    @FXML
    private Button goBackButton;
    @FXML
    private Button loginButton;

    private UserService userService = new UserService();

    @FXML
    private void onLoginClick() { // When the Login button is clicked
        String email = emailField.getText();
        String password;
        if (passwordVisibleField.isVisible()) {
            password = passwordVisibleField.getText();
        } else {
            password = passwordField.getText();
        }
        if(userService.validateLogin(email, password)) {
            System.out.println("Login successful: " + email);

            GameView gameView = new GameView(true);
            SceneManager.switchToScene(gameView);
        } else {
            System.out.println("Invalid email or password");
        }
    }

    @FXML
    private void onGoBackClick() { // When the Go Back button is clicked
        LandingPageView landingView = new LandingPageView();
        SceneManager.switchToScene(landingView);

        Stage stage = SceneManager.getPrimaryStage();
        stage.setTitle("Geofarer - Geography Learning Game");
    }

    /**
     * Navigates to the sign-up page when the link is clicked.
     */
    @FXML
    private void onSignUpLinkClick() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        // Use the PageLoader to open the SignUpPage
        PageLoader.openPage("/pages/SettingsPage.fxml", "Sign Up", stage);
    }
}