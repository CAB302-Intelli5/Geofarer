package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import views.GameView;

import java.io.IOException;

public class PageLoader {

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
}