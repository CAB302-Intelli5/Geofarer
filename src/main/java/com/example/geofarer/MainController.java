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

public class MainController {

    public void openLoginPage() { // Open the Login page
        openPage("LoginPage.fxml", "Login");
    }

    public void openSignUpPage() { // Open the Sign Up page
        openPage("SignUpPage.fxml", "Sign Up");
    }

    // Function for opening a page
    private void openPage(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            if (root instanceof BorderPane borderPane) {
                // Imports the image for the background
                BackgroundImage bgImage = new BackgroundImage(
                        new Image(getClass().getResource("/images/Map.png").toExternalForm()),
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(1.0, 1.0, true, true, false, false)
                );
                borderPane.setBackground(new Background(bgImage));
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 800, 500));
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
