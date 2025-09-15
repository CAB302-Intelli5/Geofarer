package com.example.geofarer.views;

import com.example.geofarer.controllers.GameController;
import com.example.geofarer.services.MapService;
import com.example.geofarer.utils.Constants;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.ObjectUtils;
import org.geotools.referencing.operation.transform.GeocentricTranslation;
import org.locationtech.jts.geom.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.geometry.Point2D;

import java.io.IOException;
import java.net.ContentHandler;
import java.util.ArrayList;
import java.util.List;

public class GameView extends VBox {
    private GameController controller;
    private MapService mapService;

    // FXML injected components
    @FXML private StackPane mapContainer; // The container for the entire map area
    @FXML private StackPane innerMapPane; // The actual pane holding imageView and overlay
    @FXML private ImageView imageView;
    @FXML private Pane overlay;
    @FXML private Label countryLabel;
    @FXML private  Label targetCountryLabel;

    // Map components
    private double imgWOrig = 0;
    private double imgHOrig = 0;
    private double aspectRatio = 1.0;
    private final List<MapService.FeatureInfo> featureInfos = new ArrayList<>();

    private final SimpleBooleanProperty imageReady = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty shapefileReady = new SimpleBooleanProperty(false);

    //Clipping rectange for boundaries of the map
    private Rectangle clipRect;

    public GameView() {
        this(false); // Default to immediate initialization
    }

    public GameView(boolean delayInitialization) {
        this.mapService = new MapService();
        this.controller = new GameController();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/pages/gameview.fxml"));
        fxmlLoader.setRoot(this);      // Set this instance as the root
        fxmlLoader.setController(this); // Set this instance as the controller

        try {
            fxmlLoader.load(); // Load the FXML content into this object
        } catch (IOException exception) {
            // provides an exception if the fxml file cannot be loaded
            throw new RuntimeException("Failed to load gameview.fxml", exception);
        }

        if (!delayInitialization) {
            loadMapData();
        }
    }

    private void loadFXML() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/pages/gameview.fxml"));
            fxmlLoader.setRoot(this); // Set THIS GameView instance as the root
            fxmlLoader.setController(this); // Set THIS GameView instance as the controller
            fxmlLoader.load(); // Load the FXML content into 'this'
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load gameview.fxml", exception);
        }
    }

    @FXML
    private void initialize() {
        controller.initializeController(targetCountryLabel, countryLabel, overlay, innerMapPane, mapContainer);
        // Load image async
        Task<Image> imgTask = new Task<>() {
            @Override
            protected Image call() {
                return mapService.loadRasterImage();
            }
        };
        imgTask.setOnSucceeded(e -> {
            Image raster = imgTask.getValue();
            if (raster != null) {
                imgWOrig = raster.getWidth();
                imgHOrig = raster.getHeight();
                aspectRatio = imgWOrig / imgHOrig;
                imageView.setImage(raster);
                imageReady.set(true);
            }
        });
        new Thread(imgTask).start();

        // Trigger shapefile loading
        loadMapData();


        // When both ready → render overlays
        imageReady.and(shapefileReady).addListener((obs, wasReady, nowReady) -> {
            if (nowReady) {
                renderOverlays();
            }
        });

        setupMapContainer();
        setupMapBindings();
        setupEventDelegation();
    }

    private void setupEventDelegation() {
        // Delegate zoom and pan events
        innerMapPane.setOnScroll(controller::handleScroll);
        innerMapPane.setOnMousePressed(controller::handleMousePress);
        innerMapPane.setOnMouseDragged(controller::handleMouseDrag);
        innerMapPane.setOnMouseReleased(controller::handleMouseRelease);

        // Delegate right-click for reset
        innerMapPane.setOnMouseClicked(controller::handleViewClick);
    }


    private void loadMapData() {
        Task<List<MapService.FeatureInfo>> loadTask = new Task<>() {
            @Override
            protected List<MapService.FeatureInfo> call() {
                return mapService.loadShapefileData();
            }
        };
        loadTask.setOnSucceeded(e -> {
            featureInfos.addAll(loadTask.getValue());
            shapefileReady.set(true);

            //Set up the game data
            controller.setFeatureInfos(featureInfos);
            controller.selectNewTarget();
        });
        new Thread(loadTask).start();
    }

    private void showError(String message) {
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        errorLabel.setWrapText(true);

        Button dismissBtn = new Button("Dismiss");

        VBox errorBox = new VBox(10, errorLabel, dismissBtn);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPadding(new Insets(20));
        errorBox.setStyle("-fx-background-color: white; -fx-border-color: red;");
        errorBox.setMaxWidth(400);

        StackPane errorOverlay = new StackPane(errorBox);
        errorOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");

        dismissBtn.setOnAction(e -> innerMapPane.getChildren().remove(errorOverlay)); // Remove from innerMapPane

        innerMapPane.getChildren().add(errorOverlay); // Add to innerMapPane
    }

    private void setupMapContainer() {
        clipRect = new Rectangle();
        mapContainer.setClip(clipRect);

        // Bind clip rectangle to container
        clipRect.widthProperty().bind(mapContainer.widthProperty());
        clipRect.heightProperty().bind(mapContainer.heightProperty());

        // Bind mapContainer width to 90% of scene width
        mapContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                mapContainer.prefWidthProperty().bind(mapContainer.getScene().widthProperty().multiply(0.9));
                mapContainer.maxWidthProperty().bind(mapContainer.getScene().widthProperty().multiply(0.9));
                mapContainer.minWidthProperty().bind(mapContainer.getScene().widthProperty().multiply(0.9));

                // Maintain 2:1 aspect ratio
                mapContainer.prefHeightProperty().bind(mapContainer.prefWidthProperty().divide(2));
                mapContainer.maxHeightProperty().bind(mapContainer.prefWidthProperty().divide(2));
                mapContainer.minHeightProperty().bind(mapContainer.prefWidthProperty().divide(2));
            }
        });
    }



    private void setupMapBindings() {
        innerMapPane.prefWidthProperty().bind(mapContainer.widthProperty());
        innerMapPane.prefHeightProperty().bind(mapContainer.heightProperty());
        innerMapPane.minWidthProperty().bind(mapContainer.widthProperty());
        innerMapPane.minHeightProperty().bind(mapContainer.heightProperty());
        innerMapPane.maxWidthProperty().bind(mapContainer.widthProperty());
        innerMapPane.maxHeightProperty().bind(mapContainer.heightProperty());

        // Let the image stretch to fill box (cropping handled by clipRect)
        imageView.fitWidthProperty().bind(innerMapPane.widthProperty());
        imageView.fitHeightProperty().bind(innerMapPane.heightProperty());


        //For resizing
        overlay.widthProperty().addListener((obs, oldVal, newVal) -> renderOverlays());
        overlay.heightProperty().addListener((obs, oldVal, newVal) -> renderOverlays());


    }

    /** Clear and redraw overlays (polylines) to match current overlay size and scale. */
    private void renderOverlays() {
        if (overlay == null || imgWOrig <= 0 || imgHOrig <= 0 || featureInfos.isEmpty()) {
            return;
        }

        // Clear existing shapes from feature infos
        for (MapService.FeatureInfo fi : featureInfos) {
            fi.shapes.clear();
        }

        overlay.getChildren().clear();

        double displayedW = overlay.getWidth();
        double displayedH = overlay.getHeight();


        System.out.println("Rendering overlays: " + displayedW + "x" + displayedH +
                " (original: " + imgWOrig + "x" + imgHOrig + ")");

        // Calculate scaling factors
        double scaleX = displayedW / imgWOrig;
        double scaleY = displayedH / imgHOrig;

        System.out.println("Scale factors: X=" + scaleX + ", Y=" + scaleY);

        for (MapService.FeatureInfo fi : featureInfos) {
            Geometry g = fi.geom;
            if (g == null) continue;

            if (g instanceof Polygon) {
                Polyline p = polylineForPolygon((Polygon) g, scaleX, scaleY);
                if (p != null) {
                    fi.shapes.add(p);
                    overlay.getChildren().add(p);
                }
            } else if (g instanceof MultiPolygon) {
                MultiPolygon mp = (MultiPolygon) g;
                for (int i = 0; i < mp.getNumGeometries(); i++) {
                    Geometry part = mp.getGeometryN(i);
                    if (part instanceof Polygon) {
                        Polyline p = polylineForPolygon((Polygon) part, scaleX, scaleY);
                        if (p != null) {
                            fi.shapes.add(p);
                            overlay.getChildren().add(p);
                        }
                    }
                }
            }
        }

        System.out.println("Rendered " + overlay.getChildren().size() + " polylines");
    }

    private Polyline polylineForPolygon(Polygon poly, double scaleX, double scaleY) {
        Coordinate[] coords = poly.getExteriorRing().getCoordinates();
        if (coords == null || coords.length == 0) return null;

        Polyline pl = new Polyline(); // Create the Polyline first
        List<Double> pts = pl.getPoints(); // Get the ObservableList of points

        for (Coordinate c : coords) {
            double lon = c.x;
            double lat = c.y;

            // Transform geographic coordinates to image pixel coordinates
            // Natural Earth raster is typically in geographic coordinates (-180 to 180, -90 to 90)
            double x = ((lon + 180.0) / 360.0) * imgWOrig * scaleX;
            double y = ((90.0 - lat) / 180.0) * imgHOrig * scaleY;

            pts.add(x);
            pts.add(y);
        }

        pl.setStroke(Color.rgb(0, 0, 0, 0.6));
        pl.setStrokeWidth(Math.max(Constants.MIN_STROKE_WIDTH,
                Constants.MAP_STROKE_WIDTH_FACTOR * Math.min(scaleX, scaleY)));
        pl.setMouseTransparent(true);
        return pl;
    }

    public void initializeMap() {
        if (featureInfos.isEmpty()) {
            loadMapData();

            // Force proper layout calculation
            this.applyCss();
            this.layout();

            // Force a resize simulation after a short delay
            javafx.animation.PauseTransition sizeDelay = new javafx.animation.PauseTransition(Duration.millis(500));
            sizeDelay.setOnFinished(e -> {
                // Simulate resize to force layout recalculation
                if (this.getScene() != null) {
                    Stage stage = (Stage) this.getScene().getWindow();
                    if (stage != null) {
                        // Force window to recalculate its layout
                        double width = stage.getWidth();
                        stage.setWidth(width + 1);
                        stage.setWidth(width);
                    }
                }
            });
            sizeDelay.play();
        }
    }

    // FXML event handlers - These delegate to the controller
    @FXML private void handleLoginButton() { controller.doLogin(); }
    @FXML private void handleGameModes() { controller.showGameModes(); }
    @FXML private void handleExplore() { controller.showExplore(); }
    @FXML private void handleLeaders() { controller.showLeaders(); }
    @FXML private void handleMyPassport() { controller.showMyPassport(); }

}