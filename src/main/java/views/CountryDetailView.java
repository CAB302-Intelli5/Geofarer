package views;

import controllers.CountryDetailController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class CountryDetailView extends VBox {

    private CountryDetailController controller;

    public CountryDetailView() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/CountryDetail.fxml"));

        loader.setRoot(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load CountryDetail.fxml", e);
        }

        controller = loader.getController();

        // Prevent the global dark theme from overriding the page-specific styles.
        // Remove any inherited 'theme' style class and re-apply the base stylesheet on this root
        // so its rules are applied after scene-level stylesheets (e.g. dark-theme).
        this.getStyleClass().remove("theme");
        try {
            String baseCss = getClass().getResource("/styles/base.css").toExternalForm();
            // Add base.css to this node so it has higher precedence than scene stylesheets
            if (!this.getStylesheets().contains(baseCss)) this.getStylesheets().add(baseCss);
        } catch (Exception ex) {
            // ignore if resource not found; not critical
        }
    }

    public CountryDetailController getController() {
        return controller;
    }
}