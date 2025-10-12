package model;


import java.sql.*;
import java.util.*;

public class UserStatsDAO {
    private int currentUserId = 1; // This is just a deafult user should be set on login

    //There is no need for a init of DBConnection.getInstance() as already dne

    /**
     * setter for current user id to set the current user id for user stat features
     * @param userId user id that wants to be set
     */
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    /**
     * Getter for the current user id for the passport features
     * @return an int fo the current user id
     */
    public int getCurrentUserId() {
        return currentUserId;
    }
    /**
     * Gets all counthires grouped by the continet with their master stat
     */
    public Map<String, List<CountryStats>> getCountriesGroupedByContinent() {
        Map<String, List<CountryStats>> result = new LinkedHashMap<String, List<CountryStats>>(); //Create a hashmap
        String query = """
        SELECT
            c.country_id,
            c.name,
            c.region,
            COALESCE(cm.mastery_level, 0) as mastery_level,
            COALESCE(cm.correct_guesses, 0) as correct_guesses
        FROM
            countries c
        LEFT JOIN
            country_mastery cm ON c.country_id = cm.country_id AND cm.user_id = ?
            ORDER BY c.region, c.name
        """;

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            System.out.println("DAO: Executing query for user ID: " + currentUserId);
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            //Check if the ResultSet has any data at all.
            if (!rs.isBeforeFirst()) {
                System.out.println("DAO: The query returned no rows.");
            }

            while (rs.next()) {
                String region = rs.getString("region");
                if (region == null || region.trim().isEmpty()) {
                    region = "Unknown";
                }

                String countryCode = rs.getString("country_id");
                String countryName = rs.getString("name");
                int masteryLevel = rs.getInt("mastery_level");
                int correctGuesses = rs.getInt("correct_guesses");

                // We can use a boolean flag to ensure it only prints once.
                if (result.isEmpty()) { // Only print for the first country found
                    System.out.println("DAO: Processing first row -> Country: " + countryName +
                            ", Region: " + region + ", Mastery: " + masteryLevel);
                }

                // Calculate progress: 33.33% per mastery level, max 100%
                double progress = Math.min(100.0, (masteryLevel * 100.0) / 3.0);
                boolean fullyUnlocked = masteryLevel >= 3;

                CountryStats stats = new CountryStats(
                        countryName,
                        countryCode,
                        progress,
                        fullyUnlocked,
                        masteryLevel
                );
                stats.setTotalCorrectGuesses(correctGuesses);

                result.computeIfAbsent(region, k -> new ArrayList<>()).add(stats);
            }

        } catch (SQLException e) {
            System.err.println("Error loading country stats: " + e.getMessage());
            e.printStackTrace();
        }

        return result;

    }


    /**
     * Gets stats for a specific country
     */
    public CountryStats getCountryStats(String countryCode) {
        String query = """
            SELECT 
                c.country,
                c.name,
                c.region,
                COALESCE(cm.mastery_level, 0) as mastery_level,
                COALESCE(cm.correct_guesses, 0) as correct_guesses
            FROM countries c
            LEFT JOIN country_mastery cm ON c.country = cm.country_id AND cm.user_id = ?
            WHERE c.country = ?
        """;

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentUserId);
            stmt.setString(2, countryCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int masteryLevel = rs.getInt("mastery_level");
                double progress = Math.min(100.0, (masteryLevel * 100.0) / 3.0);
                boolean fullyUnlocked = masteryLevel >= 3;

                CountryStats stats = new CountryStats(
                        rs.getString("name"),
                        rs.getString("country"),
                        progress,
                        fullyUnlocked,
                        masteryLevel
                );
                stats.setTotalCorrectGuesses(rs.getInt("correct_guesses"));
                return stats;
            }

        } catch (SQLException e) {
            System.err.println("Error loading country stats: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Updates country mastery after a successful guess
     * @param countryCode The FIPS10 country code
     * @param usedHints Whether hints were used during this round
     */
    public void recordCorrectGuess(String countryCode, boolean usedHints) {
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Get current mastery level
                String selectQuery = """
                    SELECT mastery_level, correct_guesses 
                    FROM country_mastery 
                    WHERE user_id = ? AND country_id = ?
                """;

                int currentMastery = 0;
                int currentCorrect = 0;

                try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                    selectStmt.setInt(1, currentUserId);
                    selectStmt.setString(2, countryCode);
                    ResultSet rs = selectStmt.executeQuery();

                    if (rs.next()) {
                        currentMastery = rs.getInt("mastery_level");
                        currentCorrect = rs.getInt("correct_guesses");
                    }
                }

                // Update mastery level
                int newMastery = currentMastery;
                if (!usedHints && currentMastery < 3) {
                    // Only increment if no hints used and not maxed out
                    newMastery = currentMastery + 1;
                } else if (usedHints && currentMastery > 0) {
                    // Reset mastery if hints were used (player didn't achieve hintless success)
                    newMastery = 0;
                }

                // Insert or update country_mastery
                String upsertQuery = """
                    INSERT INTO country_mastery (user_id, country_id, mastery_level, correct_guesses, last_played)
                    VALUES (?, ?, ?, ?, datetime('now'))
                    ON CONFLICT(user_id, country_id) DO UPDATE SET
                        mastery_level = ?,
                        correct_guesses = correct_guesses + 1,
                        last_played = datetime('now')
                """;

                try (PreparedStatement upsertStmt = conn.prepareStatement(upsertQuery)) {
                    upsertStmt.setInt(1, currentUserId);
                    upsertStmt.setString(2, countryCode);
                    upsertStmt.setInt(3, newMastery);
                    upsertStmt.setInt(4, currentCorrect + 1);
                    upsertStmt.setInt(5, newMastery);
                    upsertStmt.executeUpdate();
                }

                conn.commit();
                System.out.println("Updated mastery for " + countryCode + ": " + newMastery +
                        " (hints used: " + usedHints + ")");

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.err.println("Error recording correct guess: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Records a hint being viewed
     */
    public void recordHintViewed(String countryCode, int hintNumber) {
        String insertQuery = """
            INSERT OR IGNORE INTO unlocked_hints (user_id, country_id, hint_id, seen_date)
            VALUES (?, ?, ?, datetime('now'))
        """;

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

            stmt.setInt(1, currentUserId);
            stmt.setString(2, countryCode);
            stmt.setInt(3, hintNumber);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error recording hint (may already exist): " + e.getMessage());
        }
    }

    /**
     * Gets overall user statistics
     */
    public Map<String, Object> getUserOverallStats() {
        Map<String, Object> stats = new HashMap<>();

        String query = """
            SELECT 
                COUNT(*) as total_countries_played,
                SUM(CASE WHEN mastery_level >= 3 THEN 1 ELSE 0 END) as fully_unlocked,
                SUM(correct_guesses) as total_correct
            FROM country_mastery
            WHERE user_id = ?
        """;

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                stats.put("countries_played", rs.getInt("total_countries_played"));
                stats.put("fully_unlocked", rs.getInt("fully_unlocked"));
                stats.put("total_correct", rs.getInt("total_correct"));
            }

        } catch (SQLException e) {
            System.err.println("Error loading overall stats: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }

    /**
     *
     * @return
     */
    public Map<String, String> getAllCountries() {
        Map<String, String> countries = new HashMap<>();

        String query = "SELECT country, name FROM countries";

        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                countries.put(rs.getString("country"), rs.getString("name"));
            }

        } catch (SQLException e) {
            System.err.println("Error loading countries: " + e.getMessage());
            e.printStackTrace();
        }

        return countries;
    }


}
