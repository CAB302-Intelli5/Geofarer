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
    /*
    This version of getCountriesByContinent does not select only the countries the user has encountered
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

    } */

    public Map<String, List<CountryStats>> getCountriesGroupedByContinent() {
        Map<String, List<CountryStats>> result = new LinkedHashMap<String, List<CountryStats>>(); //Create a hashmap
        String query = """
        SELECT
            uc.*,
            COALESCE(cm.mastery_level, 0) as mastery_level,
            COALESCE(cm.correct_guesses, 0) as correct_guesses
        FROM
            (
                SELECT DISTINCT c.* FROM unlocked_hints uh
                JOIN hints h ON uh.hint_id = h.hint_id
                JOIN countries c ON h.country_id = c.country_id
                WHERE uh.user_id = ?
            ) uc
        LEFT JOIN
            country_mastery cm ON uc.country_id = cm.country_id AND cm.user_id = ?
            ORDER BY uc.region, uc.name
        """;

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            System.out.println("DAO: Executing query for user ID: " + currentUserId);
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
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
                c.country_id,
                c.name,
                c.region,
                COALESCE(cm.mastery_level, 0) as mastery_level,
                COALESCE(cm.correct_guesses, 0) as correct_guesses
            FROM countries c
            LEFT JOIN country_mastery cm ON c.country_id = cm.country_id AND cm.user_id = ?
            WHERE c.country_id = ?
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
                        rs.getString("country_id"),
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
    /*
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
    } */

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

    /**
     * Gets total number of hints unlocked for the user
     * @return count of hints unlocked
     */
    public int getHintsUnlocked() {
        String query = "SELECT COUNT(DISTINCT hint_id) as hints_count FROM unlocked_hints WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("hints_count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting hints unlocked: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }

    /**
     * Gets continent mastery stats for spider/radar chart
     * @return Map with continent name as key and mastery percentage as value
     */
    public Map<String, Double> getContinentMasteryStats() {
        Map<String, Double> continentStats = new LinkedHashMap<>();
        
        String query = """
            SELECT 
                c.region,
                COUNT(DISTINCT c.country_id) as total_countries,
                COUNT(DISTINCT CASE WHEN cm.mastery_level >= 3 THEN c.country_id END) as mastered_countries
            FROM countries c
            LEFT JOIN country_mastery cm ON c.country_id = cm.country_id AND cm.user_id = ?
            WHERE c.region IS NOT NULL AND c.region != ''
            GROUP BY c.region
            ORDER BY c.region
        """;
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String region = rs.getString("region");
                int total = rs.getInt("total_countries");
                int mastered = rs.getInt("mastered_countries");
                
                double percentage = total > 0 ? (mastered * 100.0) / total : 0.0;
                continentStats.put(region, percentage);
            }
        } catch (SQLException e) {
            System.err.println("Error getting continent mastery stats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return continentStats;
    }

    /**
     * Gets progression data over time (countries mastered per day/week)
     * @return List of maps containing date and count
     */
    public List<Map<String, Object>> getProgressionOverTime() {
        List<Map<String, Object>> progression = new ArrayList<>();
        
        String query = """
            SELECT 
                DATE(last_played) as play_date,
                COUNT(DISTINCT country_id) as countries_played,
                SUM(CASE WHEN mastery_level >= 3 THEN 1 ELSE 0 END) as countries_mastered
            FROM country_mastery
            WHERE user_id = ? AND last_played IS NOT NULL
            GROUP BY DATE(last_played)
            ORDER BY play_date ASC
        """;
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            int cumulativeMastered = 0;
            while (rs.next()) {
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("date", rs.getString("play_date"));
                dataPoint.put("countries_played", rs.getInt("countries_played"));
                cumulativeMastered += rs.getInt("countries_mastered");
                dataPoint.put("cumulative_mastered", cumulativeMastered);
                progression.add(dataPoint);
            }
        } catch (SQLException e) {
            System.err.println("Error getting progression data: " + e.getMessage());
            e.printStackTrace();
        }
        
        return progression;
    }

    /**
     * Calculates user rank based on countries mastered
     * @return Rank title (e.g., "Explorer", "Traveler", "Master Geographer")
     */
    public String getUserRank() {
        Map<String, Object> stats = getUserOverallStats();
        int countriesMastered = (int) stats.getOrDefault("fully_unlocked", 0);
        
        if (countriesMastered >= 150) {
            return "Master Geographer";
        } else if (countriesMastered >= 100) {
            return "World Traveler";
        } else if (countriesMastered >= 50) {
            return "Continental Expert";
        } else if (countriesMastered >= 25) {
            return "Regional Explorer";
        } else if (countriesMastered >= 10) {
            return "Adventurer";
        } else if (countriesMastered >= 5) {
            return "Novice Explorer";
        } else {
            return "Beginner";
        }
    }

    /**
     * Gets match-based progression data for a specific country
     * Returns a list of matches with their outcomes to show progression toward mastery
     * @param countryCode The FIPS10 country code
     * @return List of maps containing match number, result (win/loss), and mastery progress
     */
    public List<Map<String, Object>> getMatchProgressionForCountry(String countryCode) {
        List<Map<String, Object>> progression = new ArrayList<>();
        
        String query = """
            SELECT 
                match_result,
                used_hints,
                played_at
            FROM match_history
            WHERE user_id = ? AND country_id = ?
            ORDER BY played_at ASC
        """;
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, currentUserId);
            stmt.setString(2, countryCode);
            ResultSet rs = stmt.executeQuery();
            
            int matchNumber = 0;
            int masteryProgress = 0;  // Track current mastery progress (-100 to 300)
            int consecutiveWinsNoHints = 0;  // Track consecutive wins without hints
            
            while (rs.next()) {
                matchNumber++;
                String result = rs.getString("match_result");
                boolean usedHints = rs.getInt("used_hints") == 1;
                
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("matchNumber", matchNumber);
                dataPoint.put("result", result);
                dataPoint.put("usedHints", usedHints);
                
                // Update mastery progress based on match outcome
                if ("win".equals(result)) {
                    if (!usedHints) {
                        consecutiveWinsNoHints++;
                        masteryProgress += 34;  // Gain progress toward next mastery level
                        
                        // Cap at mastery level 3 (300%)
                        if (masteryProgress > 300) {
                            masteryProgress = 300;
                        }
                    } else {
                        // Win with hints doesn't increase mastery much
                        consecutiveWinsNoHints = 0;
                        masteryProgress += 5;
                    }
                } else {
                    // Loss reduces progress
                    consecutiveWinsNoHints = 0;
                    masteryProgress -= 25;
                    if (masteryProgress < 0) {
                        masteryProgress = 0;
                    }
                }
                
                dataPoint.put("masteryProgress", masteryProgress);
                dataPoint.put("consecutiveWins", consecutiveWinsNoHints);
                progression.add(dataPoint);
            }
        } catch (SQLException e) {
            System.err.println("Error getting match progression for country: " + e.getMessage());
            e.printStackTrace();
        }
        
        return progression;
    }

    /**
     * Records a match result (win or loss) in the match history
     * @param countryCode The FIPS10 country code
     * @param isWin Whether the match was won
     * @param usedHints Whether hints were used during this match
     */
    public void recordMatchResult(String countryCode, boolean isWin, boolean usedHints) {
        String insertQuery = """
            INSERT INTO match_history (user_id, country_id, match_result, used_hints, played_at)
            VALUES (?, ?, ?, ?, datetime('now'))
        """;

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

            stmt.setInt(1, currentUserId);
            stmt.setString(2, countryCode);
            stmt.setString(3, isWin ? "win" : "loss");
            stmt.setInt(4, usedHints ? 1 : 0);
            stmt.executeUpdate();

            System.out.println("Recorded match result for " + countryCode + ": " + 
                             (isWin ? "WIN" : "LOSS") + 
                             (usedHints ? " (with hints)" : " (no hints)"));

        } catch (SQLException e) {
            System.err.println("Error recording match result: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets aggregated match progression across all countries
     * Shows overall mastery progress based on all matches played
     * @return List of maps containing match number and cumulative mastery score
     */
    public List<Map<String, Object>> getOverallMatchProgression() {
        List<Map<String, Object>> progression = new ArrayList<>();
        
        String query = """
            SELECT 
                mh.match_result,
                mh.used_hints,
                mh.country_id,
                mh.played_at
            FROM match_history mh
            WHERE mh.user_id = ?
            ORDER BY mh.played_at ASC
        """;
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            int matchNumber = 0;
            int totalMasteryScore = 0;  // Cumulative mastery score across all countries
            int countriesMastered = 0;
            
            // Track mastery progress per country
            Map<String, Integer> countryMasteryProgress = new HashMap<>();
            Map<String, Integer> countryConsecutiveWins = new HashMap<>();
            
            while (rs.next()) {
                matchNumber++;
                String result = rs.getString("match_result");
                boolean usedHints = rs.getInt("used_hints") == 1;
                String countryId = rs.getString("country_id");
                
                // Get current progress for this country
                int currentProgress = countryMasteryProgress.getOrDefault(countryId, 0);
                int consecutiveWins = countryConsecutiveWins.getOrDefault(countryId, 0);
                int previousMasteryLevel = currentProgress / 100;
                
                // Update progress based on match outcome
                if ("win".equals(result)) {
                    if (!usedHints) {
                        consecutiveWins++;
                        currentProgress += 34;
                        if (currentProgress > 300) {
                            currentProgress = 300;
                        }
                    } else {
                        consecutiveWins = 0;
                        currentProgress += 5;
                    }
                } else {
                    consecutiveWins = 0;
                    // Losing resets progress to 0, creating a big drop if you were close to mastering
                    currentProgress = 0;
                }
                
                // Update country tracking
                countryMasteryProgress.put(countryId, currentProgress);
                countryConsecutiveWins.put(countryId, consecutiveWins);
                
                // Check if country just reached mastery level 3
                int newMasteryLevel = currentProgress / 100;
                if (newMasteryLevel >= 3 && previousMasteryLevel < 3) {
                    countriesMastered++;
                }
                
                // Calculate total mastery score
                totalMasteryScore = countryMasteryProgress.values().stream()
                        .mapToInt(Integer::intValue)
                        .sum();
                
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("matchNumber", matchNumber);
                dataPoint.put("totalMasteryScore", totalMasteryScore);
                dataPoint.put("countriesMastered", countriesMastered);
                dataPoint.put("result", result);
                
                progression.add(dataPoint);
            }
        } catch (SQLException e) {
            System.err.println("Error getting overall match progression: " + e.getMessage());
            e.printStackTrace();
        }
        
        return progression;
    }


}
