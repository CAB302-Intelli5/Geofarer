package com.example.geofarer.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.geofarer.views.GameView;

import java.io.IOException;

public class PageLoader {

    public static void openPage(String fxmlPath, String title, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(PageLoader.class.getResource(fxmlPath));
            Parent root = loader.load();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openGameView(String title, Stage stage) {
        GameView gameView = new GameView(); // GameView now loads its own FXML internally
        stage.setTitle(title);
        stage.setScene(new Scene(gameView, Constants.DEFAULT_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_HEIGHT)); // Set initial size
        stage.show();
        gameView.initializeMap(); // Call map initialization after showing the stage
    }
}