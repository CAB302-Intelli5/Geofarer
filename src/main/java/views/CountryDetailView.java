package views;

import controllers.CountryDetailController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class CountryDetailView extends BorderPane {

    private CountryDetailController controller;

    public CountryDetailView() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/CountryDetail.fxml"));

        try {
            BorderPane content = loader.load();
            
            // Copy all regions from the loaded BorderPane
            this.setTop(content.getTop());
            this.setCenter(content.getCenter());
            this.setBottom(content.getBottom());
            this.setLeft(content.getLeft());
            this.setRight(content.getRight());
            
            // Copy styles
            this.getStyleClass().addAll(content.getStyleClass());
            this.getStylesheets().addAll(content.getStylesheets());
            
            // Copy size preferences
            this.setPrefWidth(content.getPrefWidth());
            this.setPrefHeight(content.getPrefHeight());
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load CountryDetail.fxml", e);
        }

        controller = loader.getController();
    }

    public CountryDetailController getController() {
        return controller;
    }
}