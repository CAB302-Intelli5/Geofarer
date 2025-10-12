package controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import model.CountryStats;
import utils.PageLoader;
import utils.SceneManager;

import java.security.SecureRandom;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class CountryDetailController {

	private static final int DEFAULT_TOTAL_HINTS = 6;
	private static final Random RANDOM = new SecureRandom();

	@FXML
	private Label countryNameLabel;
	@FXML
	private Label continentLabel;
	@FXML
	private Label progressValueLabel;
	@FXML
	private Label progressBadge;
	@FXML
	private ProgressBar progressBar;
	@FXML
	private VBox hintsContainer;
	@FXML
	private VBox lockedHintsContainer;
	@FXML
	private Button backButton;

	private Runnable backHandler;
	private int totalHintSlots = DEFAULT_TOTAL_HINTS;

	@FXML
	private void initialize() {
		if (hintsContainer != null) {
			hintsContainer.getChildren().clear();
		}

		if (lockedHintsContainer != null) {
			lockedHintsContainer.getChildren().clear();
		}

		showHints(Collections.emptyList(), totalHintSlots);
	}

	public void displayCountry(CountryStats country, String continentName) {
		if (country == null) {
			countryNameLabel.setText("Unknown Country");
			continentLabel.setText("");
			updateProgress(0.0, 0);
			return;
		}

		countryNameLabel.setText(country.getCountryName());
		continentLabel.setText(continentName != null ? continentName : "");

		double progress = Math.max(0.0, Math.min(100.0, country.getProgress()));
		updateProgress(progress, country.getMasteryLevel());
	}

	public void showHints(List<String> unlockedHints, int totalHints) {
		totalHintSlots = Math.max(DEFAULT_TOTAL_HINTS, totalHints);

		List<String> hints = unlockedHints != null ? new ArrayList<>(unlockedHints) : Collections.emptyList();

		if (hintsContainer != null) {
			hintsContainer.getChildren().clear();
			if (hints.isEmpty()) {
				Label emptyState = new Label("No hints unlocked yet. Keep exploring to discover more!");
				emptyState.getStyleClass().add("locked-hint-cta");
				hintsContainer.getChildren().add(emptyState);
			} else {
				int index = 1;
				for (String hint : hints) {
					hintsContainer.getChildren().add(buildHintCard(hint, index++));
				}
			}
		}

		int lockedCount = Math.max(0, totalHintSlots - hints.size());

		if (lockedHintsContainer != null) {
			lockedHintsContainer.getChildren().clear();
			for (int i = 0; i < lockedCount; i++) {
				lockedHintsContainer.getChildren().add(buildLockedHintCard(hints.size() + i + 1));
			}
		}
	}

	public void showHints(List<String> unlockedHints) {
		showHints(unlockedHints, totalHintSlots);
	}

	public void setBackHandler(Runnable handler) {
		this.backHandler = handler;
	}

	@FXML
	private void handleBackAction() {
		if (backHandler != null) {
			backHandler.run();
		} else {
			if (SceneManager.getPrimaryStage() != null) {
				PageLoader.openPassportView("My Passport", SceneManager.getPrimaryStage());
			}
		}
	}

	private void updateProgress(double progressValue, int masteryLevel) {
		if (progressBar != null) {
			progressBar.setProgress(progressValue / 100.0);
		}

		if (progressValueLabel != null) {
			NumberFormat percentFormat = NumberFormat.getPercentInstance(Locale.getDefault());
			percentFormat.setMaximumFractionDigits(0);
			progressValueLabel.setText(percentFormat.format(progressValue / 100.0));
		}

		if (progressBadge != null) {
			if (progressValue >= 100) {
				progressBadge.setText("Fully Unlocked");
			} else {
				progressBadge.setText(String.format("Mastery %d/3", Math.max(0, Math.min(3, masteryLevel))));
			}
		}
	}

	private Node buildHintCard(String hintText, int index) {
		String displayText = hintText != null && !hintText.trim().isEmpty() ? hintText.trim() : "Hint " + index;

		VBox card = new VBox(8);
		card.getStyleClass().add("hint-card");
		card.setMaxWidth(500);

		Label title = new Label(String.format("Hint %d", index));
		title.getStyleClass().add("hint-index");

		Label body = new Label(displayText);
		body.setWrapText(true);
		body.getStyleClass().add("hint-text");

		// Add a subtle icon or indicator for unlocked hints
		Label status = new Label("✓ Unlocked");
		status.getStyleClass().add("hint-status");

		card.getChildren().addAll(title, body, status);
		return card;
	}

	private Node buildLockedHintCard(int index) {
		VBox card = new VBox(8);
		card.getStyleClass().add("locked-hint-card");
		card.setMaxWidth(500);

		Label header = new Label(String.format("Hint %d", index));
		header.getStyleClass().add("locked-hint-header");

		Label redacted = new Label(generateRedactedText());
		redacted.getStyleClass().add("locked-hint-redacted");
		redacted.setWrapText(true);

		Label cta = new Label("🔒 Play to unlock this hint");
		cta.getStyleClass().add("locked-hint-cta");

		card.getChildren().addAll(header, redacted, cta);
		return card;
	}

	private String generateRedactedText() {
		int lines = 2 + RANDOM.nextInt(3); // 2-4 lines of redacted text
		StringBuilder builder = new StringBuilder();

		for (int line = 0; line < lines; line++) {
			// Create black bars of varying lengths (like redacted sensitive information)
			int barLength = 20 + RANDOM.nextInt(30); // 20-50 characters wide
			for (int i = 0; i < barLength; i++) {
				builder.append('█'); // Unicode black square block
			}
			if (line < lines - 1) {
				builder.append('\n');
			}
		}

		return builder.toString();
	}
}
