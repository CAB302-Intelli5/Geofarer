package controllers;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import model.SettingsService;
import utils.PageLoader;
import javafx.fxml.FXML;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import utils.SceneManager;
import utils.StyleManager;
import utils.SessionManager;
import utils.UIUtils;

public class SettingsController extends BaseController {
    @FXML
    private GridPane settingsGrid;
    @FXML
    private ToggleButton themeToggled;
    @FXML
    private CheckBox largeTextChecked;
    @FXML
    private CheckBox dysFontChecked;
    @FXML
    private CheckBox CVDModeChecked;
    @FXML
    private Button saveButton;
    @FXML
    private Button discardButton;
    private Scene scene;

    private final SettingsService settings = SettingsService.getInstance();

    @FXML
    public void initialize() {
        super.initialize();
        // toggling button updating theme Observer pattern
        themeToggled.selectedProperty().addListener((obs, oldTheme, newTheme) -> {
            if (newTheme) {
                settings.setTheme(SettingsService.ThemeType.LIGHT);
                themeToggled.setText("Switch to Dark Mode");
            } else {
                settings.setTheme(SettingsService.ThemeType.DARK);
                themeToggled.setText("Switch to Light Mode");
            }
        });

        // binding the checkboxes to the Singleton properties
        largeTextChecked.selectedProperty().bindBidirectional(settings.largeTextProperty());
        dysFontChecked.selectedProperty().bindBidirectional(settings.dysFontProperty());
        CVDModeChecked.selectedProperty().bindBidirectional(settings.CVDModeProperty());

        // initialising Theme state and text
        themeToggled.setSelected(settings.getTheme() == SettingsService.ThemeType.LIGHT);
        // Set button label to reflect the action that will occur when clicked
        if (settings.getTheme() == SettingsService.ThemeType.LIGHT) {
            themeToggled.setText("Switch to Dark Mode");
        } else {
            themeToggled.setText("Switch to Light Mode");
        }
    }

    @FXML
    private Button loginButton;

    /**
     * Handle the login button click. If user is logged in, show the account menu anchored
     * to the login button (profile/settings/logout). Otherwise open the login page.
     */
    @FXML
    private void handleLoginButton() {
        if (SessionManager.getInstance().isLoggedIn()) {
            UIUtils.showAccountMenu(loginButton);
        } else {
            // Open login page via BaseController helper
            openLoginPage();
        }
    }

    @FXML
    protected void onGoBackClick() { // When the logo or website title button is clicked
        Stage stage = SceneManager.getPrimaryStage();
        PageLoader.openPage("/pages/LandingPage.fxml", "Geofarer - Geography Learning Game", stage);
    }
}

