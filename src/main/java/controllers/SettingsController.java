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

    public void init() {
        // toggling button updating theme with Observer pattern
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
        if (settings.getTheme() == SettingsService.ThemeType.LIGHT) {
            themeToggled.setText("Switch to Light Mode");
        } else {
            themeToggled.setText("Switch to Dark Mode");
        }
    }

    @FXML
    protected void onGoBackClick() { // When the logo or website title button is clicked
        Stage stage = SceneManager.getPrimaryStage();
        PageLoader.openPage("/pages/LandingPage.fxml", "Geofarer - Geography Learning Game", stage);
    }
}

