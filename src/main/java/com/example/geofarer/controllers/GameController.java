package com.example.geofarer.controllers;

import com.example.geofarer.services.MapService;
import hints.HintsClient;
import javafx.fxml.FXML;

import javafx.scene.control.TextArea;

import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;

import javafx.scene.layout.StackPane;
import javafx.scene.transform.Affine;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class GameController {
    @FXML
    private Label targetCountryLabel;
    @FXML Label countryLabel;
    private Pane overlay;
    private StackPane innerMapPane;
    private StackPane mapContainer;
    private TextArea hintsTextArea;

    private String targetCountry = "Unknown";
    private List<MapService.FeatureInfo> featureInfos;

    // Zoom and Pan State
    private double zoomLevel = 1.0;
    private static final double MIN_ZOOM = 1.0;
    private static final double MAX_ZOOM = 5.0;
    private static final double ZOOM_FACTOR = 1.2;
    private double lastPanX = 0;
    private double lastPanY = 0;
    private boolean isPanning = false;
    private boolean dragDetected = false;

    private String clickedCountryCode = "XXX";


    @FXML
    public void initializeController(Label targetCountryLabel, Label countryLabel, TextArea hintsTextArea, Pane overlay, StackPane innerMapPane, StackPane mapContainer) {
        // Use "this." to refer to the instance variables of the GameController class
        this.targetCountryLabel = targetCountryLabel;
        this.countryLabel = countryLabel;
        this.hintsTextArea = hintsTextArea;
        this.overlay = overlay;
        this.innerMapPane = innerMapPane;
        this.mapContainer = mapContainer;

        if (this.targetCountryLabel != null) {
            targetCountryLabel.setText("Target Country: Loading...");
        }
        if (this.countryLabel != null) {
            countryLabel.setText("Click on a country to see its name");
        }

        if(this.hintsTextArea != null) {
            hintsTextArea.setText("Hints go here.");
        }
    }
    public void setFeatureInfos(List<MapService.FeatureInfo> featureInfos) {
        this.featureInfos = featureInfos;
        selectRandomTargetCountry(); // Call this after data is set
    }

    public void processMapClick(MouseEvent event) {

        double displayedW = overlay.getWidth();
        double displayedH = overlay.getHeight();

        if (displayedW <= 0 || displayedH <= 0) {
            System.err.println("Overlay dimensions not ready for click handling.");
            return;
        }
        javafx.geometry.Point2D localCoords = innerMapPane.sceneToLocal(event.getSceneX(), event.getSceneY());
                                
        //The inner pane is designed to be seperate and therefore we get the width and height
        //Get the Coords from click
        double clickX = localCoords.getX();
        double clickY = localCoords.getY();


        //Now I need to convert these UI coords to the WGS84 which natrual earth uses
        double lon = (clickX / displayedW) * 360 - 180.0; //Formula to convert to longitude
        double lat = 90.0 - (clickY / displayedH) * 180.0; //Formula for lat

        //Using the Java Topology Suite (JTS) fund the country
        GeometryFactory geomFactory = JTSFactoryFinder.getGeometryFactory();
        Point clickedPoint = geomFactory.createPoint(new Coordinate(lon, lat));

        //Now we have the point lets finds the country that contains the clicked.

        String clickedCountry = "Unknown";
        for (MapService.FeatureInfo fi: featureInfos) {
            if (fi.geom.contains(clickedPoint)){
                clickedCountry = fi.name;
                clickedCountryCode = fi.fips10;
                System.out.println(clickedCountry);
                break; // Found the country, stop searching
            }
        }
        System.out.println(clickedCountryCode);
        if (clickedCountryCode.equals("XXX")) return; // ignore unknown click
        
        if (clickedCountry.equals("Unknown")){
            return; // ignore this click
        }

        //Update the country label
        if (clickedCountry.equals(targetCountry)) {
            countryLabel.setText("Success! You clicked " + targetCountry);
        } else {
            countryLabel.setText("Failed: You clicked: " +clickedCountry + ". Here is a hint!");
            hintsTextArea.setText("Hint 1: " +findHint());
        }
    }
    private String findHint() {
        try {
            HintsClient hintsClient = new HintsClient();
            return hintsClient.findAllGermany();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(); // Log or show alert if needed
            return "Error loading country data.";
        }
    }

    private void selectRandomTargetCountry() {
        if (featureInfos == null || featureInfos.isEmpty()) {
            targetCountry = "Unknown";
            targetCountryLabel.setText("Target Country: " + targetCountry);
            return;
        }
        Random random = new Random();
        int index = random.nextInt(featureInfos.size());
        targetCountry = featureInfos.get(index).name;
        targetCountryLabel.setText("Target Country: " +targetCountry);
    }


    // Method to start a new round with a different target country
    public void selectNewTarget() {
        selectRandomTargetCountry();
        if (countryLabel != null) {
            countryLabel.setText("Click on a country to see its name");
        }
    }

    // --- Zoom and Pan Logic ---

    public void handleScroll(ScrollEvent event) {
        event.consume();
        double deltaY = event.getDeltaY();
        if (deltaY == 0) return;

        double scaleFactor = (deltaY > 0) ? ZOOM_FACTOR : 1 / ZOOM_FACTOR;
        double newZoom = zoomLevel * scaleFactor;
        newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newZoom));

        if (newZoom != zoomLevel) {
            zoomAroundPoint(newZoom, event.getX(), event.getY());
        }
    }

    public void handleMousePress(MouseEvent event) {
        if (event.isPrimaryButtonDown()) {
            dragDetected = false;
            lastPanX = event.getX();
            lastPanY = event.getY();
            isPanning = true;
            innerMapPane.setCursor(Cursor.CLOSED_HAND);
            event.consume();
        }
    }

    public void handleMouseDrag(MouseEvent event) {
        if (isPanning && event.isPrimaryButtonDown()) {
            double deltaX = event.getX() - lastPanX;
            double deltaY = event.getY() - lastPanY;
            dragDetected = true;
            pan(deltaX, deltaY);
            lastPanX = event.getX();
            lastPanY = event.getY();
            event.consume();
        }
    }

    public void handleMouseRelease(MouseEvent event) {
        if (isPanning && !dragDetected) {
            processMapClick(event);
        }
        // Reset all states
        isPanning = false;
        dragDetected = false;
        innerMapPane.setCursor(Cursor.DEFAULT);
        event.consume();
    }

    public void handleViewClick(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            resetZoomAndPan();
            event.consume();
            System.out.println("Right click detected");
        }
    }

    private void zoomAroundPoint(double newZoom, double pivotX, double pivotY) {
        double currentTranslateX = 0;
        double currentTranslateY = 0;
        if (!innerMapPane.getTransforms().isEmpty()) {
            javafx.scene.transform.Transform currentTransform = innerMapPane.getTransforms().get(0);
            currentTranslateX = currentTransform.getTx();
            currentTranslateY = currentTransform.getTy();
        }
        double scaleFactor = newZoom / zoomLevel;
        double newTranslateX = pivotX - (pivotX - currentTranslateX) * scaleFactor;
        double newTranslateY = pivotY - (pivotY - currentTranslateY) * scaleFactor;
        zoomLevel = newZoom;
        Affine newTransform = new Affine();
        newTransform.prependScale(zoomLevel, zoomLevel);
        newTransform.prependTranslation(newTranslateX, newTranslateY);
        applyTransformWithBounds(newTransform);
    }

    private void pan(double deltaX, double deltaY) {
        javafx.scene.transform.Transform currentTransform = innerMapPane.getTransforms().isEmpty() ?
                new Affine() : innerMapPane.getTransforms().get(0);
        Affine newTransform = new Affine(currentTransform);
        newTransform.prependTranslation(deltaX, deltaY);
        applyTransformWithBounds(newTransform);
    }

    private void applyTransformWithBounds(Affine transform) {
        // Get the dimensions of the container (the viewport)
        final double containerWidth = mapContainer.getWidth();
        final double containerHeight = mapContainer.getHeight();

        // Get the dimensions of the content (the map) at the current zoom level
        final double contentWidth = containerWidth * zoomLevel;
        final double contentHeight = containerHeight * zoomLevel;

        double translateX = transform.getTx();
        double translateY = transform.getTy();

        //Lets create a bounding box

        double minTranslateX = containerWidth - contentWidth;
        double maxTranslateX = 0;

        double minTranslateY = containerHeight - contentHeight;
        double maxTranslateY = 0;

        //Keep translation between the bounds
        translateX = Math.max(minTranslateX, Math.min(maxTranslateX, translateX));
        translateY = Math.max(minTranslateY, Math.min(maxTranslateY, translateY));


        Affine boundedTransform = new Affine();
        boundedTransform.prependScale(zoomLevel, zoomLevel);
        boundedTransform.prependTranslation(translateX, translateY);
        innerMapPane.getTransforms().clear();
        innerMapPane.getTransforms().add(boundedTransform);
    }

    public void resetZoomAndPan() {
        zoomLevel = 1.0;
        innerMapPane.getTransforms().clear();
        innerMapPane.setTranslateX(0);
        innerMapPane.setTranslateY(0);
    }


    // Navigation Logic
    public void doLogin() { System.out.println("Login button clicked!"); }
    public void showGameModes() { System.out.println("Game Modes clicked!"); }
    public void showExplore() { System.out.println("Explore clicked!"); }
    public void showLeaders() { System.out.println("Leaders clicked!"); }
    public void showMyPassport() { System.out.println("My Passport clicked!"); }

    // Getter methods for accessing current game state
    public String getTargetCountry() {
        return targetCountry;
    }

    public List<MapService.FeatureInfo> getFeatureInfos() {
        return featureInfos;
    }
}
