package controllers;

import javafx.fxml.FXML;
import javafx.scene.text.Font;
import utils.PageLoader;
import utils.SceneManager;
import views.GameView;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class LandingPageController extends BaseController {

    @FXML
    private Button loginButton;

    @FXML
    public void initialize() {
        Font.loadFont(getClass().getResource("/fonts/Inika-Regular.ttf").toExternalForm(), 72);
        Font.loadFont(getClass().getResource("/fonts/Inika-Bold.ttf").toExternalForm(), 72);
    }
    // Method for the View to pass the button reference
    public void setLoginButton(Button loginButton) {
        this.loginButton = loginButton;
    }

    /**
     * Handles the click event for the login button.
     * Navigates the user to the login page.
     */

    public void onLoginButtonClick() {
        if (loginButton != null && loginButton.getScene() != null && loginButton.getScene().getWindow() != null) {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            PageLoader.openPage("/pages/LoginPage.fxml", "Login", stage);
        } else {
            System.err.println("Could not get the stage from the login button.");
        }
    }

    public void startGame() {
        System.out.println("Play button clicked, initializing game view");
        GameView gameView = new GameView(true);
        SceneManager.switchToScene(gameView);
    }

    public void showLogin() { }
    public void showRegister() { }
    public void showStats() { }
}