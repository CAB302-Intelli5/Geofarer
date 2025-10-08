package views;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import controllers.UserStatsController;

import java.io.IOException;

public class UserStatsView extends VBox {
    private UserStatsController controller;

    /**
     * Constructor that loads the UserStats view using FXML
     */
    public UserStatsView() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/UserStatsPage.fxml"));
        loader.setRoot(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load UserStatsPage.fxml", e);
        }

        controller = loader.getController();
    }

    /**
     * Gets the controller associated with this view
     * @return UserStatsController instance
     */
    public UserStatsController getController() {
        return controller;
    }

    /**
     * Initializes the user stats by loading data
     */
    public void initializeUserStats() {
        if (controller != null) {
            controller.loadUserStats();
        }
    }
}
