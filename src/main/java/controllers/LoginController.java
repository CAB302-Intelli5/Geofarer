package controllers;

import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import model.UserService;
import utils.SceneManager;
import views.GameView;
import views.LandingPageView;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import utils.PageLoader;
import javafx.scene.control.Button;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class LoginController extends BaseController {

    @FXML
    private Button goBackButton;
    @FXML
    private Button loginButton;

    @FXML
    private ImageView userImageView;

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
        Stage stage = SceneManager.getPrimaryStage();
        PageLoader.openPage("/pages/LandingPage.fxml", "Geofarer - Geography Learning Game", stage);
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

    @FXML
    public void initialize() {
        setupUserMenu();
        Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/userIcon.png")));
        userImageView.setImage(img);

        userImageView.setPreserveRatio(true);
        userImageView.setSmooth(true);
    }
}