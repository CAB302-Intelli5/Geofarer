package controllers;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.CountryStats;
import model.HintsManager;
import model.UserStatsDAO;
import utils.PageLoader;
import javafx.stage.Stage;
import utils.SessionManager;


import java.util.*;


public class PassportController {
    @FXML private VBox statsContainer;
    @FXML private  VBox mapViewContainer;
    @FXML private ToggleButton viewToggle;
    @FXML private ComboBox <String> continentFilter;
    @FXML private Button loginButton;
    @FXML private Label overallStatsLabel;

    private boolean isStatsView = true;
    private Map<String, List<CountryStats>> countriesByContinent;
    private String selectedContinent = "All";
    private UserStatsDAO userStatsDAO; // get the userStatsDAO model
    private HintsManager hintsManager;

    //Colours of the page to match the passport theme
    private static final String PASSPORT_GREEN = "#A6A186";
    private static final String PASSPORT_CREAM = "#E4F2F1";
    private static final String PASSPORT_PEACH = "#F2C3A7";
    private static final String PASSPORT_RED= "#731A12";
    private static final String PASSPORT_DARK= "#0D1A26";
    private static final int TOTAL_HINT_SLOTS = 6;

    @FXML
    public void initialize() {
        userStatsDAO = new UserStatsDAO();

        // Set current user ID from session
        if (SessionManager.getInstance().isLoggedIn()) {
            Integer userId = SessionManager.getInstance().getCurrentUserId();
            if (userId != null) {
                userStatsDAO.setCurrentUserId(userId);
                System.out.println("PassportController: Loaded for user ID: " + userId);
            }
        }
        setupViewToggle();
        setupContinentFilter();
        //Set the view to have the stats showing first
        statsContainer.setVisible(true);
        mapViewContainer.setVisible(false);
        loadUserStats();
    }

    private void setupViewToggle() {
        viewToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            isStatsView = !newVal;
            statsContainer.setVisible(isStatsView);
            mapViewContainer.setVisible(!isStatsView);

            //Check if stats or map
            if(isStatsView){
                viewToggle.setText("Stats View");
            } else {
                viewToggle.setText("Map View");
                loadMapView();
            }
        });
    }

    private  void setupContinentFilter() {
        //Add the continents from the shapefile
        continentFilter.getItems().addAll("All", "Africa", "Asia", "Europe",
                "North America", "South America",
                "Oceania", "Antarctica");

        continentFilter.setValue("All");
        //Change the continet filter if user requests
        continentFilter.setOnAction(e -> {
            selectedContinent = continentFilter.getValue();
            refreshStatsView();
        });

    }

    /**
     * Loads the user stats from the database
     */
    public void loadUserStats() {
        // Verify user is still logged in
        if (!SessionManager.getInstance().isLoggedIn()) {
            System.out.println("User not logged in, cannot load stats");
            System.out.println("CONTROLLER: loadUserStats() called, but SessionManager reports user is NOT logged in. Aborting database call.");

            // Show message in stats container
            if (statsContainer != null) {
                statsContainer.getChildren().clear();
                Label loginPrompt = new Label("Please log in to view your passport");
                loginPrompt.setStyle("-fx-font-size: 18px; -fx-text-fill: " + PASSPORT_CREAM +
                        "; -fx-padding: 50;");
                statsContainer.getChildren().add(loginPrompt);
            }
            return;
        }

        System.out.println("CONTROLLER: SessionManager reports user is logged in. Proceeding to call DAO.");

        // Load user statistics from database
        countriesByContinent = userStatsDAO.getCountriesGroupedByContinent();

        // Load overall stats
        Map<String, Object> overallStats = userStatsDAO.getUserOverallStats();
        updateOverallStatsDisplay(overallStats);

        refreshStatsView();
    }

    private void updateOverallStatsDisplay(Map<String, Object> stats) {
        if (overallStatsLabel != null && stats != null) {
            int countriesPlayed = (Integer) stats.getOrDefault("countries_played", 0);
            int fullyUnlocked = (Integer) stats.getOrDefault("fully_unlocked", 0);
            int totalCorrect = (Integer) stats.getOrDefault("total_correct", 0);

            String statsText = String.format(
                    "Countries Played: %d | Fully Unlocked: %d | Total Correct: %d",
                    countriesPlayed, fullyUnlocked, totalCorrect
            );
            overallStatsLabel.setText(statsText);
        }
    }

    private void refreshStatsView() {
        statsContainer.getChildren().clear();
        statsContainer.setFillWidth(true);

        if (countriesByContinent == null || countriesByContinent.isEmpty()) {
            Label noDataLabel = new Label("No statistics yet. Play some games to see your progress!");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: " + PASSPORT_CREAM + "; -fx-padding: 50;");
            statsContainer.getChildren().add(noDataLabel);
            return;
        }

        System.out.println("CONTROLLER: Found data for continents: " + countriesByContinent.keySet());

        List<String> continentsToShow = new ArrayList<>();
        if (selectedContinent.equals("All")) {
            continentsToShow.addAll(countriesByContinent.keySet());
        } else if (countriesByContinent.containsKey(selectedContinent)) {
            continentsToShow.add(selectedContinent);
        }

        Collections.sort(continentsToShow);

        for (String continent : continentsToShow) {
            List<CountryStats> countries = countriesByContinent.get(continent);
            if (countries == null || countries.isEmpty()) continue;

            VBox continentSection = createContinentSection(continent, countries);
            statsContainer.getChildren().add(continentSection);
            System.out.println("CONTROLLER: Successfully created and added UI section for continent: " + continent);
        }
    }

    private VBox createContinentSection(String continentName, List<CountryStats> countries) {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15, 20, 15, 20));
        section.setStyle("-fx-background-color: " + PASSPORT_CREAM + ";" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + PASSPORT_GREEN + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 8;");

        VBox.setMargin(section, new Insets(10, 15, 10, 15));

        // Continent header with dropdown arrow
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-cursor: hand;");

        Label arrow = new Label("▼");
        arrow.setStyle("-fx-font-size: 16px; -fx-text-fill: " + PASSPORT_RED + ";");

    Label continentLabel = new Label(utils.TextUtils.stripHtmlTags(continentName));
        continentLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; " +
                "-fx-text-fill: " + PASSPORT_DARK + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Calculate continent progress
        int totalCountries = countries.size();
        int completedCountries = (int) countries.stream()
                .filter(CountryStats::isFullyUnlocked)
                .count();
        double continentProgress = totalCountries > 0 ?
                (completedCountries * 100.0 / totalCountries) : 0;

        Label progressLabel = new Label(String.format("%d/%d (%.0f%%)",
                completedCountries,
                totalCountries,
                continentProgress));
        progressLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-text-fill: " + PASSPORT_RED + ";");

        header.getChildren().addAll(arrow, continentLabel, spacer, progressLabel);

        // Progress bar for continent
        ProgressBar continentBar = new ProgressBar(continentProgress / 100.0);
        continentBar.setPrefWidth(Double.MAX_VALUE);
        continentBar.setPrefHeight(8);
        continentBar.setStyle("-fx-accent: " + PASSPORT_RED + ";");

        // Container for country list
        VBox countryList = new VBox(8);
        countryList.setPadding(new Insets(10, 0, 0, 0));

        // Sort countries alphabetically
        countries.sort(Comparator.comparing(CountryStats::getCountryName));

        // Add countries
        for (CountryStats country : countries) {
            HBox countryRow = createCountryRow(country, continentName);
            countryList.getChildren().add(countryRow);
        }

        // Toggle visibility - start collapsed for cleaner view
        countryList.setVisible(false);
        countryList.setManaged(false);

        header.setOnMouseClicked(e -> {
            boolean isVisible = countryList.isVisible();
            countryList.setVisible(!isVisible);
            countryList.setManaged(!isVisible);
            arrow.setText(isVisible ? "▼" : "▲");
        });

        section.getChildren().addAll(header, continentBar, countryList);

        return section;
    }

    private HBox createCountryRow(CountryStats country, String continentName) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 5;" +
                "-fx-border-color: " + PASSPORT_GREEN + ";" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 5;");

        // Stamp or lock icon
        StackPane stampContainer = new StackPane();
        stampContainer.setPrefSize(50, 50);
        stampContainer.setMinSize(50, 50);
        stampContainer.setMaxSize(50, 50);

        if (country.isFullyUnlocked()) {
            // Show passport stamp with checkmark
            Circle stampCircle = new Circle(25);
            stampCircle.setFill(Color.web(PASSPORT_RED, 0.2));
            stampCircle.setStroke(Color.web(PASSPORT_RED));
            stampCircle.setStrokeWidth(2.5);

            Label stampText = new Label("✓");
            stampText.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; " +
                    "-fx-text-fill: " + PASSPORT_RED + ";");

            stampContainer.getChildren().addAll(stampCircle, stampText);
        } else {
            // Show lock icon
            Label lockIcon = new Label("🔒");
            lockIcon.setStyle("-fx-font-size: 28px;");
            stampContainer.getChildren().add(lockIcon);
        }

        // Country info container
        VBox infoBox = new VBox(3);
        infoBox.setMinWidth(200);

    Label countryName = new Label(utils.TextUtils.stripHtmlTags(country.getCountryName()));
        countryName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + PASSPORT_DARK + ";");

        // Mastery level indicator (stars)
        Label masteryStars = new Label(country.getMasteryStars());
        masteryStars.setStyle("-fx-font-size: 14px; -fx-text-fill: " + PASSPORT_RED + ";");

        infoBox.getChildren().addAll(countryName, masteryStars);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Progress indicator
        VBox progressContainer = new VBox(3);
        progressContainer.setAlignment(Pos.CENTER_RIGHT);
        progressContainer.setMinWidth(120);

        ProgressBar progressBar = new ProgressBar(country.getProgress() / 100.0);
        progressBar.setPrefWidth(100);
        progressBar.setPrefHeight(10);
        progressBar.setStyle("-fx-accent: " + PASSPORT_RED + ";");

        Label progressText = new Label(String.format("%.0f%%", country.getProgress()));
        progressText.setStyle("-fx-font-size: 12px; -fx-text-fill: " + PASSPORT_DARK + ";");

        progressContainer.getChildren().addAll(progressBar, progressText);

        row.getChildren().addAll(stampContainer, infoBox, spacer, progressContainer);

        // Hover effects
        row.setOnMouseEntered(e -> {
            row.setStyle(row.getStyle().replace("-fx-background-color: white;",
                    "-fx-background-color: " + PASSPORT_CREAM + ";"));
        });
        row.setOnMouseExited(e -> {
            row.setStyle(row.getStyle().replace("-fx-background-color: " + PASSPORT_CREAM + ";",
                    "-fx-background-color: white;"));
        });

        // Make clickable for future detail view
        row.setOnMouseClicked(e -> openCountryDetail(country, continentName));

        return row;
    }

    private void openCountryDetail(CountryStats country, String continentName) {
        if (country == null || statsContainer == null || statsContainer.getScene() == null) {
            return;
        }

        Stage stage = (Stage) statsContainer.getScene().getWindow();
        if (stage == null) {
            return;
        }

        System.out.println("Opening detail view for country: " + country.getCountryName());

        // Get unlocked hints from database
        this.hintsManager = new HintsManager(country.getCountryCode(), country.getCountryName());
        List<String> unlockedHints = hintsManager.getUnlockedHints(country.getCountryCode());

        PageLoader.openCountryDetailView(
                String.format("Passport - %s", country.getCountryName()),
                stage,
                country,
                continentName,
                unlockedHints,
                TOTAL_HINT_SLOTS
        );
    }

    /*
    /**
     * Calculate which hints should be unlocked for a country based on its progress
     */
    /*
    private List<String> getUnlockedHintsForCountry(CountryStats country) {
        List<String> unlockedHints = new ArrayList<>();

        // If country is fully unlocked, show all hints
        if (country.isFullyUnlocked() || country.getProgress() >= 100) {
            try {
                HintsManager hintsManager = new HintsManager(country.getCountryCode().toLowerCase(), country.getCountryName());
                // Show all 6 hints when fully unlocked
                for (int i = 0; i < TOTAL_HINT_SLOTS; i++) {
                    unlockedHints.add(hintsManager.showNextHint(i));
                }
            } catch (Exception e) {
                System.err.println("Error loading hints for country: " + country.getCountryCode());
                e.printStackTrace();
            }
            return unlockedHints;
        }

        // Calculate number of hints to unlock based on progress
        double progress = country.getProgress();
        int hintsToUnlock = 0;

        if (progress >= 84) hintsToUnlock = 5;
        else if (progress >= 67) hintsToUnlock = 4;
        else if (progress >= 51) hintsToUnlock = 3;
        else if (progress >= 34) hintsToUnlock = 2;
        else if (progress >= 17) hintsToUnlock = 1;
        // else hintsToUnlock = 0

        // Load the hints that should be unlocked
        if (hintsToUnlock > 0) {
            try {
                HintsManager hintsManager = new HintsManager(country.getCountryCode().toLowerCase(), country.getCountryName());
                for (int i = 0; i < hintsToUnlock; i++) {
                    unlockedHints.add(hintsManager.showNextHint(i));
                }
            } catch (Exception e) {
                System.err.println("Error loading hints for country: " + country.getCountryCode());
                e.printStackTrace();
            }
        }

        return unlockedHints;
    }*/

    private void loadMapView() {
        mapViewContainer.getChildren().clear();

        Label placeholder = new Label("Map View - Coming Soon");
        placeholder.setStyle("-fx-font-size: 24px; -fx-text-fill: " + PASSPORT_CREAM + ";");

        Label description = new Label("Interactive map showing your progress by country");
        description.setStyle("-fx-font-size: 14px; -fx-text-fill: " + PASSPORT_PEACH + "; -fx-font-style: italic;");

        VBox content = new VBox(20, placeholder, description);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(50));

        mapViewContainer.getChildren().add(content);
    }

    @FXML
    public void handleLoginButton() {
        if (loginButton == null) return;

        if (SessionManager.getInstance().isLoggedIn()) {
            // Show account menu with logout/profile
            utils.UIUtils.showAccountMenu(loginButton);
            return;
        }

        Stage stage = (Stage) loginButton.getScene().getWindow();
        PageLoader.openPage("/pages/LoginPage.fxml", "Login", stage);
    }

    @FXML
    public void handleGameModes() {
        // Navigate back to the main game view
        if (statsContainer == null || statsContainer.getScene() == null) {
            System.out.println("Cannot open game view: scene/statsContainer is null");
            return;
        }
        Stage stage = (Stage) statsContainer.getScene().getWindow();
        PageLoader.openGameView("Geofarer - Geography Game", stage);
    }

    @FXML
    public void handleExplore() {
        System.out.println("Explore clicked!");
    }

    @FXML
    public void handleLeaders() {
        System.out.println("Leaders clicked!");
    }

    @FXML
    public void handleMyPassport() {
        System.out.println("My Passport clicked!");
    }

    @FXML
    public void handleMyStats() {
        Stage stage = (Stage) statsContainer.getScene().getWindow();
        PageLoader.openUserStatsView("My Stats - Geofarer", stage);
    }

    @FXML
    public void handleBackToGame() {
        Stage stage = (Stage) statsContainer.getScene().getWindow();
        PageLoader.openGameView("Geofarer - Geography Game", stage);
    }
}
