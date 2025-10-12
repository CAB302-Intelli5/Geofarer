package utils;

import model.MapService;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Handles the change of scenes throughout the project whilst within a page. Thus allowing
 * for one page to handle multiple scenes such as a popup or change in the map
 */
public class SceneManager {
    private static Stage primaryStage;
    private static MapService mapService;

    /**
     * Initialises the stage to the screen
     * @param stage the stage that wants to be initalsised (primary stage)
     */
    public static void initialise(Stage stage) {
        primaryStage = stage;
        mapService = new MapService();
    }

    /**
     * Function that switches the scene the user sees on the page. Changing any fxml data and scene data necessary
     * @param root the root of the pane
     */
    public static void switchToScene(Pane root) {
        System.out.println("SceneManager: switchToScene called");
        if (primaryStage == null) {
            System.err.println("SceneManager: primaryStage is null!");
            return;
        }
        Scene currentScene = primaryStage.getScene();
        if (currentScene == null) {
            // Creaete scene if no scene
            primaryStage.setScene(new Scene(root)); //default dimensions
        } else {
            primaryStage.setScene(new Scene(root, currentScene.getWidth(), currentScene.getHeight())); //Uses last scenes dimensions
        }
        primaryStage.show();
    }


    /**
     * Preloads the large raster image and shapefile so that it does not slow down the output of the scene
     */
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

    /**
     * Getter to find what the primary stage currently on the screen is
     * @return The current primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}