package controllers;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import utils.PageLoader;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import utils.SceneManager;
import views.LandingPageView;

public class SettingsController extends BaseController {
    public GridPane settingsGrid;
    public Button resetPasswordButton;
    public CheckBox dyslexiaFontChecked;
    public CheckBox largeTextChecked;
    public Button saveSettingsButton;


    public SettingsController() {
    }


    @FXML
    public void initialize() {
    }

    @FXML
    protected void onGoBackClick() { // When the logo or website title button is clicked
        Stage stage = SceneManager.getPrimaryStage();
        PageLoader.openPage("/pages/LandingPage.fxml", "Geofarer - Geography Learning Game", stage);
    }

    @FXML
    protected void onPasswordReset(ActionEvent actionEvent) {
        Stage stage = SceneManager.getPrimaryStage();
        PageLoader.openPage("/pages/LandingPage.fxml", "Geofarer - Geography Learning Game", stage);
    }



}

