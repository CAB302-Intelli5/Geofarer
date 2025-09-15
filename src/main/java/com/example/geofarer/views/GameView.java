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

    // Map components
    private double imgWOrig = 0;
    private double imgHOrig = 0;
    private double aspectRatio = 1.0;
    private final List<MapService.FeatureInfo> featureInfos = new ArrayList<>();

    //retry tracking
    private int renderRetryCount = 0;
    private static final int MAX_RENDER_RETRIES = 20;

    private final SimpleBooleanProperty imageReady = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty shapefileReady = new SimpleBooleanProperty(false);

    //Clipping rectange for boundaries of the map
    private Rectangle clipRect;

    //Zooming Fields
    private double zoomLevel = 1.0;
    private static final double MIN_ZOOM = 1.0;
    private static final double MAX_ZOOM = 5.0;
    private static final double ZOOM_FACTOR = 1.2;

    //Panning fields
    private double lastPanX = 0;
    private double lastPanY = 0;
    private boolean isPanning = false;

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
        initialize();

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
        setupZoomAndPan();
        overlay.setOnMouseClicked(event -> {
            if (!isPanning && event.getButton() == MouseButton.PRIMARY) { // Ignore clicks if panning
                controller.handleMapClick(event, overlay.getWidth(), overlay.getHeight(), featureInfos, countryLabel);
            }
        });
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

    //Setting up zoom and pan

    private void setupZoomAndPan() {
        //Using the mouse wheel for zooming
        innerMapPane.setOnScroll(event -> {
            //We are now scrolling
            event.consume();

            double deltaY = event.getDeltaY();
            if (deltaY == 0) return;

            double scaleFactor = (deltaY > 0) ? ZOOM_FACTOR : 1/ ZOOM_FACTOR;
            double newZoom = zoomLevel * scaleFactor; //We can change teh scale factor if we want to make it quicker or slower

            //Clamp the zoom level
            newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newZoom)); //Takes the new zoom if it is between the min and max

            if (newZoom != zoomLevel) {
                //Get the nouse position relative to the innerMapPane which si where we will zoom into
                double mouseX = event.getX();
                double mouseY = event.getY();

                //Calculate that zoom
                zoomAroundPoint(newZoom, mouseX, mouseY);
            }
        });

        //We now have the new zoom so lets do panning, this is done while the mouse is pressed

        innerMapPane.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                lastPanX = event.getX();
                lastPanY = event.getY();
                isPanning = true;
                innerMapPane.setCursor(Cursor.CLOSED_HAND); //This just makes a nice graphic for us to use (hopefully)
                event.consume();;
            }
        });

        //We now know the if mouse is pressed we now need to know if it is dragged
        //Here there is mouse dragged exit, I think it should be live even if it is more compyutationally heavy
        innerMapPane.setOnMouseDragged(event -> {
            if(isPanning && event.isPrimaryButtonDown()) {
                double deltaX = event.getX() - lastPanX;
                double deltaY = event.getY() - lastPanY;

                pan(deltaX, deltaY); //Call the pan function

                lastPanX = event.getX();
                lastPanY = event.getY();
                event.consume();
            }
        });

        //Stop panning on mouse release
        innerMapPane.setOnMouseReleased(event -> {
            if (isPanning) {
                isPanning = false;
                innerMapPane.setCursor(Cursor.DEFAULT);
                event.consume();
            }
        });

        //lets have right click resetting the zoom back to normal
        innerMapPane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY){
                resetZoomAndPan();
                event.consume();
                System.out.println("Right click detected");
            }
        });
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

        //calculate the new transform
        javafx.scene.transform.Affine newTransform = new javafx.scene.transform.Affine();

        //zoom arounnd the mouse point
        newTransform.prependScale(zoomLevel, zoomLevel);
        newTransform.prependTranslation(newTranslateX, newTranslateY);

        //Check the bounds
        applyTransormWithBounds(newTransform);
    }

    //Panning function that updates the view
    private void pan(double deltaX, double deltaY) {
        //Get current transform
        javafx.scene.transform.Transform currentTransform = innerMapPane.getTransforms().isEmpty() ?
                new javafx.scene.transform.Affine() :
                innerMapPane.getTransforms().get(0);

        //create a new transform
        javafx.scene.transform.Affine newTransform = new javafx.scene.transform.Affine(currentTransform);
        newTransform.prependTranslation(deltaX, deltaY);

        applyTransormWithBounds(newTransform);

    }

    //Function that actually applies the transform
    private void applyTransormWithBounds(javafx.scene.transform.Affine transform) {
        //Get the bouynds of the content
        // double contentWidth = mapContainer.getWidth() * zoomLevel;
        // double contentHeight = mapContainer.getHeight() * zoomLevel;
        // double containerWidth = mapContainer.getWidth();
        // double containerHeight = mapContainer.getHeight();

        //Get translation values
        double translateX = transform.getTx();
        double translateY = transform.getTy();

        // //only apply the bounds if content is larger than container
        // if (contentWidth > containerWidth) {
        //     double maxTranslateX = 0;
        //     double minTranslateX = (containerWidth - contentWidth) / zoomLevel;
        //     translateX = Math.max(minTranslateX, Math.min(maxTranslateX, translateX)); //Same logic as zooming
        // } else {
        //     translateX = (containerWidth - contentWidth) / (2 * zoomLevel); //Center the conent
        // }
        // if (contentHeight > containerHeight) {
        //     double maxTranslateY = 0;
        //     double minTranslateY = (containerHeight - contentHeight) / zoomLevel;
        //     translateY = Math.max(minTranslateY, Math.min(maxTranslateY, translateY));
        // } else {
        //     //Cennter contertn
        //     translateY = (containerHeight - contentHeight) / (2 * zoomLevel);
        // }
        //Apply the transform
        javafx.scene.transform.Affine boundedTransform = new javafx.scene.transform.Affine();
        boundedTransform.prependScale(zoomLevel, zoomLevel);
        boundedTransform.prependTranslation(translateX, translateY);

        innerMapPane.getTransforms().clear();
        innerMapPane.getTransforms().add(boundedTransform);
    }

    //Resets the zoom and pan back to original
    private void resetZoomAndPan() {
        zoomLevel = 1.0;
        innerMapPane.getTransforms().clear();
        innerMapPane.setTranslateX(0);
        innerMapPane.setTranslateY(0);
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