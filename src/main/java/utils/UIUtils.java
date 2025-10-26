package utils;

import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

/**
 * Small UI utility helpers shared across controllers.
 */
public class UIUtils {

    /**
     * Shows a small account menu anchored to the provided button.
     * Menu contains "Profile" (opens user stats) and "Logout" (clears session and returns to landing).
     */
    public static void showAccountMenu(Button anchor) {
        if (anchor == null || anchor.getScene() == null) return;

        ContextMenu menu = new ContextMenu();
        MenuItem profile = new MenuItem("Profile");
        MenuItem logout = new MenuItem("Logout");

        profile.setOnAction(e -> {
            Stage stage = (Stage) anchor.getScene().getWindow();
            if (stage != null) {
                PageLoader.openUserStatsView("My Stats - Geofarer", stage);
            }
        });

        logout.setOnAction(e -> {
            // Clear session
            SessionManager.getInstance().logout();

            // After logout, navigate back to landing page (refreshes header state)
            Stage stage = (Stage) anchor.getScene().getWindow();
            if (stage != null) {
                PageLoader.openPage("/pages/LandingPage.fxml", "Geofarer - Geography Learning Game", stage);
            }
        });

        menu.getItems().addAll(profile, logout);

        // Show below the button
        menu.show(anchor, Side.BOTTOM, 0, 0);
    }
}
