package controllers;

import utils.PageLoader;
import utils.SceneManager;
import views.GameView;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controller for the landing page of the applications.
 * Handles the navigation from the landign page to the login, register, stats
 * and the init of the game view
 */
public class LandingPageController {

    private Button loginButton;

    /**
     *Refernce  to the login button from the view
     * @param loginButton This login button in the view
     */
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

    /**
     * Is called to start the game in the by initializing a new game view
     */
    public void startGame() {
        System.out.println("Play button clicked, initializing game view");
        GameView gameView = new GameView(true);
        SceneManager.switchToScene(gameView);
    }

    public void showLogin() { }
    public void showRegister() { }
    public void showStats() { }
}