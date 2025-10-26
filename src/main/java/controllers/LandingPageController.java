package controllers;

import javafx.fxml.FXML;
import javafx.scene.text.Font;
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

    @FXML
    private Button loginButton;

    @FXML
    public void initialize() {
        Font.loadFont(getClass().getResource("/fonts/Inika-Regular.ttf").toExternalForm(), 72);
        Font.loadFont(getClass().getResource("/fonts/Inika-Bold.ttf").toExternalForm(), 72);
    }
    // Method for the View to pass the button reference
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
        if (loginButton == null || loginButton.getScene() == null || loginButton.getScene().getWindow() == null) {
            System.err.println("Could not get the stage from the login button.");
            return;
        }

        // If logged in show account menu, otherwise open login page
        if (utils.SessionManager.getInstance().isLoggedIn()) {
            utils.UIUtils.showAccountMenu(loginButton);
        } else {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            PageLoader.openPage("/pages/LoginPage.fxml", "Geofarer - Geography Learning Game", stage);
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