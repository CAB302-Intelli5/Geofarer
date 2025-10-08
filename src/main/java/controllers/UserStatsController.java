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
        
        // Set colors
        plot.setSeriesPaint(0, java.awt.Color.decode(PASSPORT_PEACH));
        plot.setSeriesOutlinePaint(0, java.awt.Color.decode(PASSPORT_RED));
        
        JFreeChart chart = new JFreeChart("Continent Mastery", 
                                         JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        chart.setBackgroundPaint(java.awt.Color.decode(PASSPORT_DARK));
        chart.getTitle().setPaint(java.awt.Color.decode(PASSPORT_CREAM));

        // Embed in JavaFX
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(600, 400));
        
        SwingNode swingNode = new SwingNode();
        SwingUtilities.invokeLater(() -> swingNode.setContent(chartPanel));
        
        spiderChartContainer.getChildren().add(swingNode);
    }

    /**
     * Creates and loads the progression line chart
     */
    private void loadProgressionChart() {
        List<Map<String, Object>> progressionData = userStatsDAO.getProgressionOverTime();
        
        if (progressionData.isEmpty()) {
            Label noDataLabel = new Label("No progression data yet. Keep playing!");
            noDataLabel.setStyle("-fx-text-fill: " + PASSPORT_CREAM + "; -fx-font-size: 14px;");
            progressionChartContainer.getChildren().add(noDataLabel);
            return;
        }

        // Create axes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Countries Mastered");

        // Create line chart
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Your Progress Over Time");
        lineChart.setLegendVisible(false);
        
        // Create series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Mastered Countries");
        
        for (Map<String, Object> dataPoint : progressionData) {
            String date = (String) dataPoint.get("date");
            Integer cumulative = (Integer) dataPoint.get("cumulative_mastered");
            series.getData().add(new XYChart.Data<>(date, cumulative));
        }
        
        lineChart.getData().add(series);
        
        // Style the chart
        lineChart.setStyle("-fx-background-color: transparent;");
        lineChart.lookup(".chart-plot-background").setStyle("-fx-background-color: rgba(230, 242, 241, 0.05);");
        
        progressionChartContainer.getChildren().add(lineChart);
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
        
        // Style the chart
        barChart.setStyle("-fx-background-color: transparent;");
        barChart.lookup(".chart-plot-background").setStyle("-fx-background-color: rgba(230, 242, 241, 0.05);");
        
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
