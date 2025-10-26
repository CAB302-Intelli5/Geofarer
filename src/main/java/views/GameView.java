package views;

import controllers.GameController;
import model.MapService;
import utils.Constants;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
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
    @FXML private TextArea hintsTextArea;
    @FXML private Label targetCountryLabel;
    @FXML private Button viewSuccessButton;

    // Map components
    private double imgWOrig = 0;
    private double imgHOrig = 0;
    private double aspectRatio = 1.0;
    private final List<MapService.FeatureInfo> featureInfos = new ArrayList<>();

    private final SimpleBooleanProperty imageReady = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty shapefileReady = new SimpleBooleanProperty(false);

    //Clipping rectangle for boundaries of the map
    private Rectangle clipRect;

    @FXML private Button loginButton;

    public GameView() {
        this(false); // Default to immediate initialization
    }

    public GameView(boolean delayInitialization) {
        this.mapService = new MapService();
        this.controller = new GameController();
        this.controller.setGameView(this);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/pages/gameview.fxml"));
        fxmlLoader.setRoot(this);      // Set this instance as the root
        fxmlLoader.setController(this); // Set this instance as the controller

        try {
            fxmlLoader.load(); // Load the FXML content into this object
        } catch (IOException exception) {
            // provides an exception if the fxml file cannot be loaded
            throw new RuntimeException("Failed to load gameview.fxml", exception);
        }

        controller.initializeController(targetCountryLabel,countryLabel, hintsTextArea,overlay,innerMapPane,mapContainer,viewSuccessButton);

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
        controller.initializeController(targetCountryLabel, countryLabel, hintsTextArea, overlay, innerMapPane, mapContainer, viewSuccessButton);

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
                checkAndRenderOverlays(); // Add this
            }
        });
        new Thread(imgTask).start();

        // Trigger shapefile loading
        loadMapData();

        setupMapContainer();
        setupMapBindings();
        setupEventDelegation();
    }

    private void checkAndRenderOverlays() {
        if (imageReady.get() && shapefileReady.get()) {
            System.out.println("DEBUG: Both ready, rendering overlays");
            renderOverlays();
        } else {
            System.out.println("DEBUG: Not ready yet - imageReady: " + imageReady.get() + ", shapefileReady: " + shapefileReady.get());
        }
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
            checkAndRenderOverlays();
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

        // Bind mapContainer width to 70% of scene width
        mapContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                mapContainer.prefWidthProperty().bind(mapContainer.getScene().widthProperty().multiply(0.7));
                mapContainer.maxWidthProperty().bind(mapContainer.getScene().widthProperty().multiply(0.7));
                mapContainer.minWidthProperty().bind(mapContainer.getScene().widthProperty().multiply(0.7));

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
                Path p = pathForPolygon((Polygon) g, scaleX, scaleY); // Use Path instead of Polyline
                if (p != null) {
                    fi.shapes.add(p);
                    overlay.getChildren().add(p);
                }
            } else if (g instanceof MultiPolygon) {
                MultiPolygon mp = (MultiPolygon) g;
                for (int i = 0; i < mp.getNumGeometries(); i++) {
                    Geometry part = mp.getGeometryN(i);
                    if (part instanceof Polygon) {
                        Path p = pathForPolygon((Polygon) part, scaleX, scaleY); // Use Path
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

    private Path pathForPolygon(Polygon poly, double scaleX, double scaleY) {
        Path path = new Path();
        path.setFill(Color.TRANSPARENT); // Default fill transparent
        path.setStroke(Color.rgb(0, 0, 0, 0.6));
        path.setStrokeWidth(Math.max(Constants.MIN_STROKE_WIDTH,
                Constants.MAP_STROKE_WIDTH_FACTOR * Math.min(scaleX, scaleY)));
        path.setMouseTransparent(true); // Clicks go through to the underlying image

        // Exterior ring
        addCoordinatesToPath(path, poly.getExteriorRing().getCoordinates(), scaleX, scaleY);

        // Interior rings (holes)
        for (int i = 0; i < poly.getNumInteriorRing(); i++) {
            addCoordinatesToPath(path, poly.getInteriorRingN(i).getCoordinates(), scaleX, scaleY);
        }

        return path;
    }

    private void addCoordinatesToPath(Path path, Coordinate[] coords, double scaleX, double scaleY) {
        if (coords == null || coords.length == 0) return;

        // Move to the first point
        Coordinate firstCoord = coords[0];
        double firstX = ((firstCoord.x + 180.0) / 360.0) * imgWOrig * scaleX;
        double firstY = ((90.0 - firstCoord.y) / 180.0) * imgHOrig * scaleY;
        path.getElements().add(new javafx.scene.shape.MoveTo(firstX, firstY));

        // Draw lines to subsequent points
        for (int i = 1; i < coords.length; i++) {
            Coordinate c = coords[i];
            double x = ((c.x + 180.0) / 360.0) * imgWOrig * scaleX;
            double y = ((90.0 - c.y) / 180.0) * imgHOrig * scaleY;
            path.getElements().add(new javafx.scene.shape.LineTo(x, y));
        }
        path.getElements().add(new javafx.scene.shape.ClosePath());
    }


    /**
     * Highlights the shapes associated with an incorrectly guessed country's geometry in red.
     * @param guessedGeometry The JTS Geometry of the guessed country.
     */
    public void highlightGuess(Geometry guessedGeometry, boolean correctGuess) {
        Platform.runLater(() -> {
            for (MapService.FeatureInfo fi : featureInfos) {
                if (fi.geom != null && fi.geom.equals(guessedGeometry)) {
                    for (Shape shape : fi.shapes) {
                        if (correctGuess != true){ //highlights as red if incorrect guess
                            shape.setFill(Color.RED.deriveColor(1, 1, 1, 0.5)); // Semi-transparent red
                            shape.setStroke(Color.DARKRED);
                        }else{ //highlights as green if correct guess
                            shape.setFill(Color.GREEN.deriveColor(1, 1, 1, 0.5));
                            shape.setStroke(Color.DARKGREEN);
                        }
                        shape.setStrokeWidth(Math.max(Constants.MIN_STROKE_WIDTH,
                                Constants.MAP_STROKE_WIDTH_FACTOR * Math.min(overlay.getWidth() / imgWOrig, overlay.getHeight() / imgHOrig)) * 2); // Thicker border
                    }
                    break; // Assuming one FeatureInfo per geometry
                }
            }
        });
    }

    /**
     * Resets the fill and stroke of all country shapes to their default (transparent fill, black stroke).
     */
    public void clearGuesses() {
        Platform.runLater(() -> {
            for (MapService.FeatureInfo fi : featureInfos) {
                for (Shape shape : fi.shapes) {
                    shape.setFill(Color.TRANSPARENT);
                    shape.setStroke(Color.rgb(0, 0, 0, 0.6));
                    shape.setStrokeWidth(Math.max(Constants.MIN_STROKE_WIDTH,
                            Constants.MAP_STROKE_WIDTH_FACTOR * Math.min(overlay.getWidth() / imgWOrig, overlay.getHeight() / imgHOrig)));
                }
            }
        });
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
    @FXML private void handleLoginButton() { controller.doLogin(loginButton); }
    @FXML private void handleGameModes() { controller.showGameModes(); }
    @FXML private void handleExplore() { controller.showExplore(); }
    @FXML private void handleLeaders() { controller.showLeaders(); }
    @FXML private void handleMyPassport() { controller.showMyPassport(); }
    @FXML private void handleSuccessButton(){controller.handleSuccessButton(viewSuccessButton);}
}