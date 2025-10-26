package utils;

import model.MapService;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class SceneManager {
    private static Stage primaryStage;
    private static MapService mapService;

    public static void initialise(Stage stage) {
        primaryStage = stage;
        mapService = new MapService();
    }
    public static void switchToScene(Pane root) {
        System.out.println("SceneManager: switchToScene called.");
        if (primaryStage == null) {
            System.err.println("SceneManager: primaryStage is null!");
            return;
        }
        Scene currentScene = primaryStage.getScene();
        Scene newScene;
        if (currentScene == null) {
            // Create scene if no scene
            newScene = new Scene(root); //default dimensions
        } else {
            newScene = new Scene(root, currentScene.getWidth(), currentScene.getHeight()); //Uses last scenes dimensions
        }

        // activating/registering the scene to StyleManager to update style
        StyleManager.getInstance().activateScene(newScene);
        primaryStage.setScene(newScene);
        primaryStage.show();
    }


    public static void preloadResources() {
        Task<Void> preloadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Preload map image
                mapService.loadRasterImage();

                // Preload shapefile data
                mapService.loadShapefileData();

                return null;
            }
        };

        new Thread(preloadTask).start();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}