package views;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import controllers.PassportController;

import java.io.IOException;

public class PassportView extends VBox {
    private PassportController controller; //Reference the controller for the view

    /**
     * This enables the passport view and loads using the fxml loader
     */
    public PassportView() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/PassportPage.fxml"));

        loader.setRoot(this);


        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load PassportPage.fxml", e);
        }
    }

        /**
         * Passport controller getter
         */
        public PassportController getController() {
            return controller;
        }

    /**
     * Calls the method to load the user stats with reference to the passport.
     */
    public void initializePassport() {
            if (controller != null) {
                controller.loadUserStats(); // Call to a method to load the user stats
            }
    }

}
