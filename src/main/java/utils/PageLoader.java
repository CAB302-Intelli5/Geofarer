package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import model.CountryStats;
import views.CountryDetailView;
import views.GameView;
import views.PassportView;
import views.UserStatsView;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Designed to open a new page and load all the fxml file data
 */
public class PageLoader {

    /**
     * Opens the given page applying all fxml data to the stage
     * @param fxmlPath FXML file taht is being accessed
     * @param title Title of that page that is being accessed
     * @param stage The stage that will be displayed on the screen
     */
    public static void openPage(String fxmlPath, String title, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(PageLoader.class.getResource(fxmlPath));
            Parent root = loader.load();
            stage.setTitle(title);
            SceneManager.switchToScene((Pane) root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openGameView(String title, Stage stage) {
        GameView gameView = new GameView(); // GameView now loads its own FXML internally
        stage.setTitle(title);
        SceneManager.switchToScene(gameView);
        stage.setOnShown(e -> gameView.initializeMap());
    }

    public static void openPassportView(String title, Stage stage) {
        PassportView passportView = new PassportView();
        stage.setTitle(title);
        SceneManager.switchToScene(passportView);
        stage.setOnShown(e -> passportView.initializePassport());
    }

    public static void openUserStatsView(String title, Stage stage) {
        UserStatsView userStatsView = new UserStatsView();
        stage.setTitle(title);
        SceneManager.switchToScene(userStatsView);
        stage.setOnShown(e -> userStatsView.initializeUserStats());
    }

    public static void openCountryDetailView(String title,
                                             Stage stage,
                                             CountryStats country,
                                             String continentName,
                                             List<String> unlockedHints,
                                             int totalHintSlots) {
        CountryDetailView detailView = new CountryDetailView();
        stage.setTitle(title);

        if (detailView.getController() != null) {
            detailView.getController().displayCountry(country, continentName);
            detailView.getController().showHints(
                    unlockedHints != null ? unlockedHints : Collections.emptyList(),
                    totalHintSlots);
            detailView.getController().setBackHandler(() -> openPassportView("My Passport", stage));
        }

        SceneManager.switchToScene(detailView);
    }

    public static void openCountryDetailView(String title,
                                             Stage stage,
                                             CountryStats country,
                                             String continentName,
                                             List<String> unlockedHints) {
        openCountryDetailView(title, stage, country, continentName, unlockedHints, 6);
    }

    public static void openCountryDetailView(String title,
                                             Stage stage,
                                             CountryStats country,
                                             String continentName) {
        openCountryDetailView(title, stage, country, continentName, Collections.emptyList(), 6);
    }
}