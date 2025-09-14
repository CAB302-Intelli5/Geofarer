package com.example.geofarer.views;

import com.example.geofarer.controllers.GameController;
import com.example.geofarer.services.MapService;
import com.example.geofarer.utils.Constants;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.locationtech.jts.geom.*;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.IOException;
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

    // Map components
    private double imgWOrig = 0;
    private double imgHOrig = 0;
    private double aspectRatio = 1.0;
    private final List<MapService.FeatureInfo> featureInfos = new ArrayList<>();

    // Add retry tracking
    private int renderRetryCount = 0;
    private static final int MAX_RENDER_RETRIES = 20;

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
        // FXML components are already injected at this point

        // Load and set up map image
        Image raster = mapService.loadRasterImage();
        if (raster != null) {
            imgWOrig = raster.getWidth();
            imgHOrig = raster.getHeight();
            aspectRatio = imgWOrig / imgHOrig;
            imageView.setImage(raster);
            System.out.println("Loaded raster: " + imgWOrig + "x" + imgHOrig + ", aspect ratio: " + aspectRatio);
        } else {
            System.err.println("Failed to load raster image!");
            aspectRatio = 1.8; // Default aspect ratio for world map
        }

        // Set up bindings for the map within its container
        setupMapBindings(mapContainer);

        // Click handler for the map
        innerMapPane.addEventHandler(MouseEvent.MOUSE_CLICKED,
                event -> controller.handleMapClick(event, innerMapPane.getWidth(), innerMapPane.getHeight(), featureInfos, countryLabel));
    }

    private void loadMapData() {
        // Show loading indicator
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(100, 100);
        StackPane loadingOverlay = new StackPane(progress);
        loadingOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.2);");
        innerMapPane.getChildren().add(loadingOverlay); // Add to the innerMapPane

        // Load data in background thread
        Task<List<MapService.FeatureInfo>> loadTask = new Task<>() {
            @Override
            protected List<MapService.FeatureInfo> call() {
                return mapService.loadShapefileData();
            }
        };

        loadTask.setOnSucceeded(event -> {
            featureInfos.addAll(loadTask.getValue());
            innerMapPane.getChildren().remove(loadingOverlay); // Remove from innerMapPane
            // Force redraw of map overlays
            Platform.runLater(() -> renderOverlays());
        });

        // Handle failure
        loadTask.setOnFailed(event -> {
            innerMapPane.getChildren().remove(loadingOverlay); // Remove from innerMapPane
            showError("Failed to load map data: " + loadTask.getException().getMessage());
        });

        // Start loading
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

    private void setupMapBindings(StackPane mapFrame) { // Now takes mapContainer as mapFrame
        // Calculate available space for the map
        DoubleBinding availableWidth = Bindings.createDoubleBinding(
                () -> {
                    double containerWidth = mapFrame.getWidth();
                    if (containerWidth <= 0 || containerWidth > 2000) {
                        containerWidth = Constants.DEFAULT_WINDOW_WIDTH; // Fallback
                    }
                    // Account for mapContainer padding
                    double effectiveWidth = containerWidth - mapFrame.getPadding().getLeft() - mapFrame.getPadding().getRight();
                    return Math.max(Constants.MIN_MAP_WIDTH,
                            Math.min(effectiveWidth * Constants.MAP_AREA_FACTOR, Constants.MAX_MAP_WIDTH));
                },
                mapFrame.widthProperty(), mapFrame.paddingProperty()
        );

        DoubleBinding availableHeight = Bindings.createDoubleBinding(
                () -> {
                    double containerHeight = mapFrame.getHeight();
                    if (containerHeight <= 0 || containerHeight > 1500) {
                        containerHeight = Constants.DEFAULT_WINDOW_HEIGHT; // Fallback
                    }
                    // Account for mapContainer padding
                    double effectiveHeight = containerHeight - mapFrame.getPadding().getTop() - mapFrame.getPadding().getBottom();
                    return Math.max(Constants.MIN_MAP_HEIGHT,
                            Math.min(effectiveHeight, Constants.MAX_MAP_HEIGHT));
                },
                mapFrame.heightProperty(), mapFrame.paddingProperty()
        );

        // Calculate optimal map dimensions based on aspect ratio
        DoubleBinding mapWidth = Bindings.createDoubleBinding(
                () -> {
                    double availW = availableWidth.get();
                    double availH = availableHeight.get();

                    if (availW <= 0 || availH <= 0) return Constants.MIN_MAP_WIDTH;

                    // Calculate width based on height constraint
                    double widthByHeight = availH * aspectRatio;

                    // Use the more constraining dimension
                    double finalWidth = Math.min(availW, widthByHeight);

                    return Math.max(Constants.MIN_MAP_WIDTH,
                            Math.min(finalWidth, Constants.MAX_MAP_WIDTH));
                },
                availableWidth, availableHeight
        );

        DoubleBinding mapHeight = Bindings.createDoubleBinding(
                () -> {
                    double width = mapWidth.get();
                    double height = width / aspectRatio;
                    return Math.max(Constants.MIN_MAP_HEIGHT,
                            Math.min(height, Constants.MAX_MAP_HEIGHT));
                },
                mapWidth
        );

        // Bind innerMapPane dimensions to calculated map dimensions
        innerMapPane.prefWidthProperty().bind(mapWidth);
        innerMapPane.prefHeightProperty().bind(mapHeight);
        innerMapPane.maxWidthProperty().bind(mapWidth);
        innerMapPane.maxHeightProperty().bind(mapHeight);
        innerMapPane.minWidthProperty().set(Constants.MIN_MAP_WIDTH);
        innerMapPane.minHeightProperty().set(Constants.MIN_MAP_HEIGHT);

        // Bind image view to inner pane
        imageView.fitWidthProperty().bind(innerMapPane.widthProperty());
        imageView.fitHeightProperty().bind(innerMapPane.heightProperty());

        // Bind overlay to match inner pane exactly
        overlay.prefWidthProperty().bind(innerMapPane.widthProperty());
        overlay.prefHeightProperty().bind(innerMapPane.heightProperty());
        overlay.minWidthProperty().bind(innerMapPane.minWidthProperty());
        overlay.minHeightProperty().bind(innerMapPane.minHeightProperty());
        overlay.maxWidthProperty().bind(innerMapPane.maxWidthProperty());
        overlay.maxHeightProperty().bind(innerMapPane.maxHeightProperty());

        // Redraw overlays when display size changes with debouncing
        SimpleBooleanProperty needsRedraw = new SimpleBooleanProperty(false);
        Timeline debouncer = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            if (needsRedraw.get()) {
                needsRedraw.set(false);
                Platform.runLater(this::renderOverlays);
            }
        }));
        debouncer.setCycleCount(Timeline.INDEFINITE);
        debouncer.play();

        ChangeListener<Number> sizeChangeListener = (obs, oldV, newV) -> {
            if (newV != null && !newV.equals(oldV)) {
                needsRedraw.set(true);
            }
        };

        mapWidth.addListener(sizeChangeListener);
        mapHeight.addListener(sizeChangeListener);

        // Handle window state changes
        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                if (stage != null) {
                    // Listen for window state changes
                    stage.maximizedProperty().addListener((prop, wasMax, isMax) -> {
                        Platform.runLater(() -> {
                            this.requestLayout();
                            Timeline layoutDelay = new Timeline(new KeyFrame(Duration.millis(200),
                                    evt -> renderOverlays()));
                            layoutDelay.play();
                        });
                    });

                    // Add window shown listener to ensure proper initial sizing
                    if (!stage.isShowing()) {
                        stage.setOnShown(evt -> {
                            Platform.runLater(() -> {
                                this.requestLayout();
                                // Allow more time for the initial render
                                Timeline initialSizeDelay = new Timeline(
                                        new KeyFrame(Duration.millis(100), e -> {
                                            double w = this.getWidth();
                                            double h = this.getHeight();
                                            System.out.println("Initial layout complete: " + w + "x" + h);
                                            renderOverlays();
                                        })
                                );
                                initialSizeDelay.play();
                            });
                        });
                    }
                }
            }
        });

        // Initial render after layout
        Platform.runLater(() -> {
            Timeline initialRender = new Timeline(new KeyFrame(Duration.millis(300),
                    e -> renderOverlays()));
            initialRender.play();
        });
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

        if (displayedW <= 1 || displayedH <= 1) {
            renderRetryCount++;
            System.out.println("Overlay dimensions not ready: " + displayedW + "x" + displayedH +
                    " (retry " + renderRetryCount + "/" + MAX_RENDER_RETRIES + ")");

            // Only retry if we haven't exceeded the limit
            if (renderRetryCount < MAX_RENDER_RETRIES) {
                Platform.runLater(() -> {
                    Timeline retryRender = new Timeline(new KeyFrame(Duration.millis(500),
                            e -> renderOverlays()));
                    retryRender.play();
                });
            } else {
                System.err.println("Failed to initialize overlay dimensions after " + MAX_RENDER_RETRIES + " retries");
                // Force a layout update one more time
                Platform.runLater(() -> {
                    this.requestLayout();
                    innerMapPane.requestLayout();
                    overlay.requestLayout();
                });
            }
            return;
        }

        // Reset retry count on successful render
        renderRetryCount = 0;

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

    // FXML event handlers for navigation buttons
    @FXML
    private void handleLoginButton() {
        System.out.println("Login button clicked!");
        // Call handle for jaydens login
    }

    @FXML
    private void handleGameModes() {
        System.out.println("Game Modes clicked!");
        // controller.navigateToGameModes();
    }

    @FXML
    private void handleExplore() {
        System.out.println("Explore clicked!");
        // controller.navigateToExplore();
    }

    @FXML
    private void handleLeaders() {
        System.out.println("Leaders clicked!");
        // controller.navigateToLeaders();
    }

    @FXML
    private void handleMyPassport() {
        System.out.println("My Passport clicked!");
        // controller.navigateToMyPassport();
    }
}