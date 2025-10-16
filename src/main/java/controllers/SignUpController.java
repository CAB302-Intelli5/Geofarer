package controllers;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.UserService;
import utils.PageLoader;
import utils.SceneManager;
import views.GameView;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import java.util.Objects;

/**
 * Sign up controller
 * Handles the signup  and navigation on the sign up page
 * Inherits from {@link BaseController} to access common UI elements like email and password fields.
 */
public class SignUpController extends BaseController {

    @FXML
    private Button goBackButton;

    @FXML
    private Button signupButton;

    @FXML
    private ImageView userImageView;

    @FXML
    private Label passwordRequirementsLabel;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private UserService userService = new UserService();

    @FXML
    private void onSignUpClick() { // When the Sign Up button is clicked
        String email = emailField.getText();
        String password;
        if (passwordVisibleField.isVisible()) {
            password = passwordVisibleField.getText();
        } else {
            password = passwordField.getText();
        }

        if (!isValidPassword(password)) {
            passwordRequirementsLabel.setVisible(true);
            passwordRequirementsLabel.setManaged(true);
            return;
        }

        if(userService.addUser(email, password)) {
            System.out.println("User added successfully: " + email);

            GameView gameView = new GameView(true);
            SceneManager.switchToScene(gameView);
        } else {
            System.out.println("Failed to add user");
        }
    }

    private boolean isValidPassword(String password) {
        if (password == null) return false;

        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.]).{8,256}$";
        return password.matches(passwordRegex);
    }

    @FXML
    private void onGoBackClick() { // When the Go Back button is clicked
        GameView gameView = new GameView(true);
        SceneManager.switchToScene(gameView);
    }

    @FXML
    private void onLoginLinkClick() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        // Use the PageLoader to open the SignUpPage
        PageLoader.openPage("/pages/LoginPage.fxml", "Geofarer - Geography Learning Game", stage);
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