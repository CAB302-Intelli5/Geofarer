package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * A controller for the success popup that allows handles playing again, exiting out,
 * accessing passport page and stats on the game.
 */
public class SuccessPopupController {

    @FXML private Label statsLabel;
    @FXML private Button passportButton;
    @FXML private Button playAgainButton;
    @FXML private Button closeButton;

    // Flag to communicate the result back to the GameController
    private boolean playAgainClicked = false;

    /**
     * Sets the stats message to the amount of guesses the user took so that it is updated in the fxml
     * @param message The amount of guesses the user took
     */
    public void setStatsMessage(String message) {
        statsLabel.setText(message);
    }

    /**
     * Public method for the GameController to check if "Play Again" was clicked.
     * @return true if the play again button was clicked, false otherwise.
     */
    public boolean isPlayAgainClicked() {
        return playAgainClicked;
    }

    @FXML
    private void handlePassportButton() {
        System.out.println("Passport button clicked! Closing popup.");
        // 'playAgainClicked' remains false
        closeWindow();
    }

    @FXML
    private void handlePlayAgainButton() {
        System.out.println("Play Again button clicked!");
        this.playAgainClicked = true; // Set the flag to true
        closeWindow();
    }

    @FXML
    private void handleCloseButton() {
        System.out.println("Close ('X') button clicked! Closing popup.");
        // 'playAgainClicked' remains false
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}