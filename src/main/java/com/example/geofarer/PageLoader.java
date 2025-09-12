package com.example.geofarer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class PageLoader {

    // Function for opening a new page
    public static void openPage(String fxmlFile, String title, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(PageLoader.class.getResource(fxmlFile));
            Parent root = loader.load();

            if (root instanceof BorderPane borderPane) {
                // Sets the background to an image of the map
                BackgroundImage bgImage = new BackgroundImage(
                        new Image(PageLoader.class.getResource("/images/Map.png").toExternalForm()),
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(1.0, 1.0, true, true, false, false)
                );
                borderPane.setBackground(new Background(bgImage));
            }

            Scene scene = new Scene(root, 800, 500);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
