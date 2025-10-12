package controllers;

import model.MapService;
import model.HintsManager;
import utils.PageLoader;
import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;

import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.io.IOException;

import java.util.List;
import java.util.Random;

/**
 * Controller for the main game logic and interactions which handles all user clicks,
 * guesses, zooms and pans and the success popup when getting the answer correct.
 */
public class GameController {
    @FXML
    private Label targetCountryLabel;
    @FXML Label countryLabel;
    private Pane overlay;
    private StackPane innerMapPane;
    private StackPane mapContainer;
    private TextArea hintsTextArea;
    @FXML private Button viewSuccessButton;

    private String targetCountry = "Unknown";
    private List<MapService.FeatureInfo> featureInfos;
    private HintsManager hintsManager;

    // Game State
    private static int guessCount = 1;
    private boolean roundWin = false;
    private final boolean correctGuess = true;

    // Zoom and Pan State
    private double zoomLevel = 1.0;
    private static final double MIN_ZOOM = 1.0;
    private static final double MAX_ZOOM = 20.0;
    private static final double ZOOM_FACTOR = 1.2;
    private double lastPanX = 0;
    private double lastPanY = 0;
    private boolean isPanning = false;
    private boolean dragDetected = false;

    private String clickedCountryCode = "XX";
    private String targetCountryCode = "XX";

    // Reference to the GameView to allow communication
    private views.GameView gameView;

    /**
     * A setter for the game view
     * @param gameView The gameview that is associated with this controller
     */
    public void setGameView(views.GameView gameView) {
        this.gameView = gameView;
    }

    /**
     * Handles the click on the success buton the succes popup page
     * @param button the success button is pressed
     */
    @FXML
    public void handleSuccessButton(Button button){
        System.out.println("View Success window button clicked");
        showSuccessPopup();
    }


    /**
     * Initializes the controller reference for fxml injection. Also sets
     * @param targetCountryLabel This is the fxml of the target country
     * @param countryLabel This is the label for that country
     * @param hintsTextArea Area for the hints to be outputted
     * @param overlay The overlay is for the entire map and border
     * @param innerMapPane The inner map pane is the area taht the map is going to fill
     * @param mapContainer The map container is filled with the map which is filled to the inner map pane
     * @param viewSuccessButton This is the button that will show up when the success overlay pops up
     */
    @FXML
    public void initializeController(Label targetCountryLabel, Label countryLabel, TextArea hintsTextArea, Pane overlay, StackPane innerMapPane, StackPane mapContainer, Button viewSuccessButton) {
        // Use "this." to refer to the instance variables of the GameController class
        this.targetCountryLabel = targetCountryLabel;
        this.countryLabel = countryLabel;
        this.hintsTextArea = hintsTextArea;
        this.overlay = overlay;
        this.innerMapPane = innerMapPane;
        this.mapContainer = mapContainer;
        this.viewSuccessButton = viewSuccessButton;

        viewSuccessButton.setVisible(roundWin); //hide the button to view success popup if the game hasn't been won yet

        if (this.targetCountryLabel != null) {
            targetCountryLabel.setText("Target Country: Loading...");
        }
        if (this.countryLabel != null) {
            countryLabel.setText("Click on a country to see its name");
        }

        if(this.hintsTextArea != null) {
            hintsTextArea.setText("Guess where the country is first to get a hint!");
        }
    }

    /**
     * Sets the available feature infos and selects a random target country
     * @param featureInfos List of map features
     */
    public void setFeatureInfos(List<MapService.FeatureInfo> featureInfos) {
        this.featureInfos = featureInfos;
        selectRandomTargetCountry(); // Call this after data is set
    }

    /**
     * Handles the click event on the map converting the screen coords to WGS84 which matches the natural earth
     * finding the country which was clicked and procesing the guess.
     * @param event Mouse click event
     */
    public void processMapClick(MouseEvent event) {
        if (featureInfos == null || featureInfos.isEmpty()) return; // just for unit tests.
        if (overlay == null || innerMapPane == null) {
            // Requires the map to process a click
            return;
        }

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
        Geometry clickedCountryGeometry = null; // Store the geometry
        for (MapService.FeatureInfo fi: featureInfos) {
            if (fi.geom.contains(clickedPoint)){
                clickedCountry = fi.name;
                clickedCountryCode = fi.fips10;
                clickedCountryGeometry = fi.geom;
                System.out.println(clickedCountry);
                break; // Found the country, stop searching
            }
        }
        System.out.println(clickedCountryCode);
        if (clickedCountryCode.equals("XX")) return; // ignore unknown click

        if (clickedCountry.equals("Unknown")){
            return; // ignore this click
        }
        processCountryGuess(clickedCountry);
    }

    public void processCountryGuess(String guessedCountryName) {
        if (featureInfos == null || featureInfos.isEmpty()) return;

        if (guessedCountryName == null || guessedCountryName.equals("Unknown")) {
            return; // ignore invalid guess
        }

        // Find the geometry for the guessed country
        Geometry guessedCountryGeometry = null;
        String guessedCountryCode = "XX";
        for (MapService.FeatureInfo fi : featureInfos) {
            if (fi.name.equals(guessedCountryName)) {
                guessedCountryGeometry = fi.geom;
                guessedCountryCode = fi.fips10;
                break;
            }
        }

        if (guessedCountryName.equals(targetCountry)) {
            if (countryLabel != null) {
                countryLabel.setText("Success! You clicked " + targetCountry);
            }
            if (gameView != null && guessedCountryGeometry != null) {
                gameView.highlightGuess(guessedCountryGeometry, correctGuess);
            }
            this.roundWin = true;
            if (viewSuccessButton != null) {
                viewSuccessButton.setVisible(roundWin);
                viewSuccessButton.setText("View Results");
            }
            showSuccessPopup();
        } else if (!roundWin) {
            if (countryLabel != null) {
                countryLabel.setText("Failed: You clicked: " + guessedCountryName + ". Here is a hint!");
            }
            if (hintsTextArea != null && hintsManager != null) {
                try {
                    hintsTextArea.appendText(hintsManager.showNextHint(guessCount - 1, targetCountry) + "\n");
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    System.out.println("Failed to load hint.");
                }
            }
            guessCount++;

            if (gameView != null && guessedCountryGeometry != null) {
                gameView.highlightGuess(guessedCountryGeometry, !correctGuess);
                System.out.println("clicked wrong country light it up!");
            }
        }
    }



    private void selectRandomTargetCountry() {
        if (featureInfos == null || featureInfos.isEmpty()) {
            targetCountry = "Unknown";
            if (targetCountryLabel != null) {
                targetCountryLabel.setText("Target Country: " + targetCountry);
            }
            return;
        }
        Random random = new Random();
        int index = random.nextInt(featureInfos.size());

        targetCountry = featureInfos.get(index).name;
        targetCountryCode = featureInfos.get(index).fips10;
        if (targetCountryLabel != null) {
            targetCountryLabel.setText("Target Country: " + targetCountry);
        }
        hintsManager = new HintsManager(targetCountryCode.toLowerCase(), targetCountry);
    }


    /**
     * Method to start a new round with a different target country
     */
    public void selectNewTarget() {
        this.roundWin = false;
        if (viewSuccessButton != null) {
            viewSuccessButton.setVisible(roundWin);
        }
        guessCount = 1; // reset guess count

        if (gameView !=  null) {
            gameView.clearGuesses(); // Clear fills after starting a new round
        }
       selectRandomTargetCountry();

        if (countryLabel != null) {
            countryLabel.setText("Click on a country to see its name");
        }
        if (hintsTextArea != null) {
            hintsTextArea.clear();
        }
    }

    /**
     * Handels the mouse wheel scroll to zoom in and out of the map
     * @param event This is as a mouse event scroll being zoom in action
     */
    public void handleScroll(ScrollEvent event) {
        if (innerMapPane == null) return; // nothing to scroll
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

    /**
     * Handles the mouse press to initiate panning and click detection
     * @param event On mouse click (left click)
     */
    public void handleMousePress(MouseEvent event) {
        if (innerMapPane == null) return;
        if (event.isPrimaryButtonDown()) {
            dragDetected = false;
            lastPanX = event.getX();
            lastPanY = event.getY();
            isPanning = true;
            innerMapPane.setCursor(Cursor.CLOSED_HAND);
            event.consume();
        }
    }

    /**
     * Handles the mouse dragging so that it can be registered
     * @param event Mouse button is being held down
     */
    public void handleMouseDrag(MouseEvent event) {
        if (innerMapPane == null) return;
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

    /**
     * Handles the mouse release so that it can stop dragging across the screen and
     * ensure the guess is not clicked on relase
     * @param event Mouse event (for mouse release)
     */
    public void handleMouseRelease(MouseEvent event) {
        if (innerMapPane == null) return;
        if (isPanning && !dragDetected) {
            processMapClick(event);
        }
        // Reset all states
        isPanning = false;
        dragDetected = false;
        innerMapPane.setCursor(Cursor.DEFAULT);
        event.consume();
    }

    /**
     * Handles the click for right click. It will reset the view
     * @param event Mouse event detecting the right click
     */
    public void handleViewClick(MouseEvent event) {
        if (innerMapPane == null) return;
        if (event.getButton() == MouseButton.SECONDARY) {
            resetZoomAndPan();
            event.consume();
            System.out.println("Right click detected");
        }
    }

    private void zoomAroundPoint(double newZoom, double pivotX, double pivotY) {
        if (innerMapPane == null) return;
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
        if (innerMapPane == null) return;
        javafx.scene.transform.Transform currentTransform = innerMapPane.getTransforms().isEmpty() ?
                new Affine() : innerMapPane.getTransforms().get(0);
        Affine newTransform = new Affine(currentTransform);
        newTransform.prependTranslation(deltaX, deltaY);
        applyTransformWithBounds(newTransform);
    }

    private void applyTransformWithBounds(Affine transform) {
        if (innerMapPane == null || mapContainer == null) {
            // Can't apply transform without map
            return;
        }
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

    /**
     * Setter function to reset the zoom and pan to set back to original map
     */
    public void resetZoomAndPan() {
        zoomLevel = 1.0;
        innerMapPane.getTransforms().clear();
        innerMapPane.setTranslateX(0);
        innerMapPane.setTranslateY(0);
    }


    /**
     * controller to show the success popup when the user is guessed correctly
     */
    protected void showSuccessPopup() {
        try {
            // Load the FXML file for the popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/SuccessPopup.fxml"));
            Parent root = loader.load();
            if (root == null) {
                System.out.println("Skipping popup in test mode: root is null");
                return;
            }

            // Get the controller of the popup
            SuccessPopupController popupController = loader.getController();

            //  Create the success message and pass it to the popup controller
            String message = String.format("You found %s in %d %s.",
                    targetCountry, guessCount, guessCount == 1 ? "guess" : "guesses");
            popupController.setStatsMessage(message);

            // Create a new stage (window) for the popup
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL); // Block interaction with the main window

            popupStage.initStyle(StageStyle.TRANSPARENT);
            Scene popupScene = new Scene(root);
            popupScene.setFill(Color.TRANSPARENT);

            popupStage.setScene(popupScene);
            popupStage.showAndWait(); // Show the popup and wait for it to be closed

            // After the popup is closed, start a new round only if play again clicked
            if (popupController.isPlayAgainClicked()) {
                selectNewTarget();
            }

        } catch (IOException e) {
            e.printStackTrace();
            // Fallback in case FXML fails to load
            System.err.println("Failed to load success popup FXML.");

            //Fallback just start the new game
            selectNewTarget();
        }

    }


    /**
     * Method to register the login button and change and call the scene change method
     * @param button This is the login button, when on click this is called
     */
    public void doLogin(Button button) {
        if (button == null) {
            System.out.println("Login button is null");
            return;
        }

        Scene scene = button.getScene();
        if (scene == null) {
            System.out.println("Scene is null");
            return;
        }

        System.out.println("Login button clicked");

        Stage stage = (Stage) button.getScene().getWindow();
        // Use the PageLoader to open the SignUpPage
        PageLoader.openPage("/pages/LoginPage.fxml", "Login", stage);
    }

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

    void setTargetCountry(String country) {
        this.targetCountry = country;
        if (targetCountryLabel != null) {
            targetCountryLabel.setText("Target Country: " + country);
        }

        if (featureInfos != null) {
            for (MapService.FeatureInfo fi : featureInfos) {
                if (fi.name.equals(country)) {
                    this.targetCountryCode = fi.fips10;
                    break;
                }
            }
        }
        this.hintsManager = new HintsManager(targetCountryCode.toLowerCase(), targetCountry);
    }

    /**
     * This is a function that returns a boolean if the match is a win or loss. This is
     * currently always going to return round win as our only game mode does not need to perform
     * this check but is good practice for later gameplay integration
     * @return True of False (True being game win, False being Game loss)
     */
    public boolean isRoundWin() {
        return roundWin;
    }
}
