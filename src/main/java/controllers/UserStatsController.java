package controllers;

import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.CountryStats;
import model.UserStatsDAO;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import utils.PageLoader;
import utils.SessionManager;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class UserStatsController {

    @FXML private Label stampsEarnedNumber;
    @FXML private Label totalWinsNumber;
    @FXML private Label hintsUnlockedNumber;
    @FXML private Label countriesPlayedNumber;
    @FXML private VBox masteredCountriesList;
    @FXML private Label rankNameLabel;
    @FXML private Label rankDescriptionLabel;
    @FXML private VBox spiderChartContainer;
    @FXML private VBox progressionChartContainer;
    @FXML private VBox continentBarChartContainer;
    @FXML private Button loginButton;

    private UserStatsDAO userStatsDAO;

    // Color palette constants
    private static final String PASSPORT_CREAM = "#E4F2F1";
    private static final String PASSPORT_PEACH = "#F2C3A7";
    private static final String PASSPORT_RED = "#731A12";
    private static final String PASSPORT_DARK = "#0D1A26";
    private static final String PASSPORT_GREEN = "#A6A186";

    @FXML
    public void initialize() {
        userStatsDAO = new UserStatsDAO();

        // Set current user ID from session
        if (SessionManager.getInstance().isLoggedIn()) {
            Integer userId = SessionManager.getInstance().getCurrentUserId();
            if (userId != null) {
                userStatsDAO.setCurrentUserId(userId);
                System.out.println("UserStatsController: Loaded for user ID: " + userId);
            }
        }

        loadUserStats();
    }

    /**
     * Loads all user statistics and populates the UI
     */
    public void loadUserStats() {
        loadStatCards();
        loadMasteredCountriesList();
        loadRank();
        loadCharts();
    }

    /**
     * Loads the stat cards with user data
     */
    private void loadStatCards() {
        Map<String, Object> stats = userStatsDAO.getUserOverallStats();
        
        int countriesMastered = (int) stats.getOrDefault("fully_unlocked", 0);
        int totalCorrect = (int) stats.getOrDefault("total_correct", 0);
        int countriesPlayed = (int) stats.getOrDefault("countries_played", 0);
        int hintsUnlocked = userStatsDAO.getHintsUnlocked();

        stampsEarnedNumber.setText(String.valueOf(countriesMastered));
        totalWinsNumber.setText(String.valueOf(totalCorrect));
        hintsUnlockedNumber.setText(String.valueOf(hintsUnlocked));
        countriesPlayedNumber.setText(String.valueOf(countriesPlayed));
    }

    /**
     * Loads the list of mastered countries
     */
    private void loadMasteredCountriesList() {
        masteredCountriesList.getChildren().clear();
        
        Map<String, List<CountryStats>> countriesByContinent = 
            userStatsDAO.getCountriesGroupedByContinent();
        
        int totalMastered = 0;
        for (Map.Entry<String, List<CountryStats>> entry : countriesByContinent.entrySet()) {
            for (CountryStats country : entry.getValue()) {
                if (country.isFullyUnlocked()) {
                    totalMastered++;
                    VBox stampItem = createStampItem(country);
                    masteredCountriesList.getChildren().add(stampItem);
                }
            }
        }

        if (totalMastered == 0) {
            Label emptyLabel = new Label("No countries mastered yet. Keep playing!");
            emptyLabel.setStyle("-fx-text-fill: " + PASSPORT_CREAM + "; -fx-font-size: 14px;");
            masteredCountriesList.getChildren().add(emptyLabel);
        }
    }

    /**
     * Creates a stamp item for a mastered country
     */
    private VBox createStampItem(CountryStats country) {
        VBox item = new VBox(5);
        item.getStyleClass().add("stamp-item");
        item.setPadding(new Insets(10));

        Label countryName = new Label(country.getCountryName());
        countryName.getStyleClass().add("stamp-country-name");

        Label masteryInfo = new Label("✓ Mastered (Level " + country.getMasteryLevel() + ")");
        masteryInfo.getStyleClass().add("stamp-mastery-indicator");

        item.getChildren().addAll(countryName, masteryInfo);
        return item;
    }

    /**
     * Loads and displays the user's rank
     */
    private void loadRank() {
        String rank = userStatsDAO.getUserRank();
        rankNameLabel.setText(rank);
        
        // Set description based on rank
        String description = getRankDescription(rank);
        rankDescriptionLabel.setText(description);
    }

    /**
     * Gets a description for the rank
     */
    private String getRankDescription(String rank) {
        return switch (rank) {
            case "Master Geographer" -> "You've conquered the world! Exceptional knowledge!";
            case "World Traveler" -> "Outstanding! You know the globe like the back of your hand.";
            case "Continental Expert" -> "Impressive mastery across multiple continents!";
            case "Regional Explorer" -> "Great progress! You're becoming a geography expert.";
            case "Adventurer" -> "Well done! Your journey across the world continues.";
            case "Novice Explorer" -> "Good start! Keep exploring to advance your rank.";
            default -> "Begin your journey to become a Master Geographer!";
        };
    }

    /**
     * Loads all charts
     */
    private void loadCharts() {
        loadSpiderChart();
        loadProgressionChart();
        loadContinentBarChart();
    }

    /**
     * Creates and loads the spider/radar chart for continent mastery
     */
    private void loadSpiderChart() {
        Map<String, Double> continentStats = userStatsDAO.getContinentMasteryStats();
        
        if (continentStats.isEmpty()) {
            Label noDataLabel = new Label("No data available yet. Start playing to see your progress!");
            noDataLabel.setStyle("-fx-text-fill: " + PASSPORT_CREAM + "; -fx-font-size: 14px;");
            spiderChartContainer.getChildren().add(noDataLabel);
            return;
        }

        // Create dataset for spider chart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> entry : continentStats.entrySet()) {
            dataset.addValue(entry.getValue(), "Mastery %", entry.getKey());
        }

        // Create spider web plot
        SpiderWebPlot plot = new SpiderWebPlot(dataset);
        plot.setLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
        
        // Set colors - using passport red with nearly full opacity for progress
        java.awt.Color redFill = new java.awt.Color(115, 26, 18, 250); // #731A12 with very high opacity (almost solid)
        java.awt.Color redOutline = java.awt.Color.decode(PASSPORT_RED);
        java.awt.Color darkBg = java.awt.Color.decode(PASSPORT_DARK);
        java.awt.Color white = java.awt.Color.WHITE;
        
        plot.setSeriesPaint(0, redFill);
        plot.setSeriesOutlinePaint(0, redOutline);
        plot.setSeriesOutlineStroke(0, new java.awt.BasicStroke(3.0f)); // Thicker outline
        
        // Make plot background match the page background
        plot.setBackgroundPaint(darkBg);
        plot.setBackgroundAlpha(1.0f);
        
        // Set web lines to white
        plot.setWebFilled(true);
        plot.setAxisLinePaint(white);
        plot.setAxisLineStroke(new java.awt.BasicStroke(1.0f));
        
        // Set label paint to cream color for visibility
        plot.setLabelPaint(java.awt.Color.decode(PASSPORT_CREAM));
        
        JFreeChart chart = new JFreeChart("Continent Mastery", 
                                         JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        chart.setBackgroundPaint(darkBg); // Match page background
        chart.getTitle().setPaint(java.awt.Color.decode(PASSPORT_CREAM));
        chart.setBorderVisible(false);
        chart.setPadding(new org.jfree.chart.ui.RectangleInsets(0, 0, 0, 0));

        // Embed in JavaFX with matching background
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(600, 400));
        chartPanel.setBackground(darkBg); // Match page background
        chartPanel.setBorder(null); // Remove border
        chartPanel.setOpaque(true); // Make opaque to show the dark background
        
        SwingNode swingNode = new SwingNode();
        SwingUtilities.invokeLater(() -> swingNode.setContent(chartPanel));
        
        spiderChartContainer.getChildren().add(swingNode);
    }

    /**
     * Creates and loads the progression line chart
     * Now shows match-by-match progression instead of date-based
     */
    private void loadProgressionChart() {
        List<Map<String, Object>> progressionData = userStatsDAO.getOverallMatchProgression();
        
        if (progressionData.isEmpty()) {
            Label noDataLabel = new Label("No progression data yet. Keep playing!");
            noDataLabel.setStyle("-fx-text-fill: " + PASSPORT_CREAM + "; -fx-font-size: 14px;");
            progressionChartContainer.getChildren().add(noDataLabel);
            return;
        }

        // Create axes
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Match Number");
        xAxis.setAutoRanging(true);
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Countries Mastered");
        yAxis.setAutoRanging(true);

        // Create line chart
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Mastery Progression by Match");
        lineChart.setLegendVisible(true);
        
        // Create series for countries mastered
        XYChart.Series<Number, Number> masteredSeries = new XYChart.Series<>();
        masteredSeries.setName("Countries Mastered");
        
        // Sample every nth point if there are too many matches (for performance)
        int sampleRate = Math.max(1, progressionData.size() / 100);
        
        for (int i = 0; i < progressionData.size(); i += sampleRate) {
            Map<String, Object> dataPoint = progressionData.get(i);
            Integer matchNumber = (Integer) dataPoint.get("matchNumber");
            Integer countriesMastered = (Integer) dataPoint.get("countriesMastered");
            String result = (String) dataPoint.get("result");
            
            XYChart.Data<Number, Number> point = new XYChart.Data<>(matchNumber, countriesMastered);
            masteredSeries.getData().add(point);
            
            // Add tooltip to show win/loss for this match
            if (point.getNode() != null) {
                String resultSymbol = "win".equals(result) ? "✓" : "✗";
                javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(
                    "Match " + matchNumber + ": " + resultSymbol + " " + result.toUpperCase()
                );
                javafx.scene.control.Tooltip.install(point.getNode(), tooltip);
            }
        }
        
        // Add the last point to ensure we show the final state
        if (sampleRate > 1 && progressionData.size() > 0) {
            Map<String, Object> lastPoint = progressionData.get(progressionData.size() - 1);
            Integer matchNumber = (Integer) lastPoint.get("matchNumber");
            Integer countriesMastered = (Integer) lastPoint.get("countriesMastered");
            masteredSeries.getData().add(new XYChart.Data<>(matchNumber, countriesMastered));
        }
        
        lineChart.getData().add(masteredSeries);
        
        // Style the chart with transparent background
        lineChart.setStyle("-fx-background-color: transparent;");
        if (lineChart.lookup(".chart-plot-background") != null) {
            lineChart.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent;");
        }
        
        // Apply passport red color to the line series
        lineChart.applyCss();
        lineChart.layout();
        masteredSeries.getNode().setStyle("-fx-stroke: " + PASSPORT_RED + "; -fx-stroke-width: 3px;");
        
        // Add information label
        int totalMatches = progressionData.size();
        Map<String, Object> lastData = progressionData.get(totalMatches - 1);
        int finalMastered = (Integer) lastData.get("countriesMastered");
        
        Label infoLabel = new Label(String.format(
            "Showing progression over %d matches • %d countries mastered", 
            totalMatches, finalMastered
        ));
        infoLabel.setStyle("-fx-text-fill: " + PASSPORT_CREAM + "; -fx-font-size: 12px; -fx-padding: 5 0 10 0;");
        
        progressionChartContainer.getChildren().addAll(infoLabel, lineChart);
    }

    /**
     * Creates and loads the continent bar chart
     */
    private void loadContinentBarChart() {
        Map<String, Double> continentStats = userStatsDAO.getContinentMasteryStats();
        
        if (continentStats.isEmpty()) {
            Label noDataLabel = new Label("No continent data yet. Start exploring!");
            noDataLabel.setStyle("-fx-text-fill: " + PASSPORT_CREAM + "; -fx-font-size: 14px;");
            continentBarChartContainer.getChildren().add(noDataLabel);
            return;
        }

        // Create axes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Continent");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Mastery Percentage");
        yAxis.setUpperBound(100);

        // Create bar chart
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Mastery by Continent");
        barChart.setLegendVisible(false);
        
        // Create series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        for (Map.Entry<String, Double> entry : continentStats.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        barChart.getData().add(series);
        
        // Style the chart with passport red
        barChart.setStyle("-fx-background-color: transparent;");
        barChart.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent;");
        
        // Apply passport red color to bars
        barChart.applyCss();
        barChart.layout();
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-bar-fill: " + PASSPORT_RED + ";");
            }
        }
        
        continentBarChartContainer.getChildren().add(barChart);
    }

    // Navigation handlers
    @FXML
    private void handleGameModes() {
        PageLoader.openPage("/pages/LandingPage.fxml", "Geofarer - Game Modes", 
                           (Stage) stampsEarnedNumber.getScene().getWindow());
    }

    @FXML
    private void handleExplore() {
        PageLoader.openGameView("Geofarer - Explore", 
                               (Stage) stampsEarnedNumber.getScene().getWindow());
    }

    @FXML
    private void handleLeaders() {
        // Navigate to leaderboard (not yet implemented)
        System.out.println("Navigate to Leaders - Not yet implemented");
    }

    @FXML
    private void handleMyPassport() {
        PageLoader.openPassportView("My Passport", 
                                   (Stage) stampsEarnedNumber.getScene().getWindow());
    }

    @FXML
    private void handleMyStats() {
        // Already on stats page, refresh data
        PageLoader.openUserStatsView("My Stats - Geofarer", 
                                    (Stage) stampsEarnedNumber.getScene().getWindow());
    }

    @FXML
    private void handleLoginButton() {
        if (SessionManager.getInstance().isLoggedIn()) {
            // Show user menu or logout
            System.out.println("User is logged in");
        } else {
            PageLoader.openPage("/pages/LoginPage.fxml", "Geofarer - Login", 
                              (Stage) loginButton.getScene().getWindow());
        }
    }
}
