package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import views.GameView;

import java.io.IOException;

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

}