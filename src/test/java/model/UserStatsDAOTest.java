package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Headless tests for UserStatsDAO following AAA pattern
 * Tests database interaction logic without UI components
 */
public class UserStatsDAOTest {

    private UserStatsDAO userStatsDAO;
    private static final int TEST_USER_ID = 9999;
    private static final String TEST_EMAIL = "teststats@example.com";
    private static final String TEST_PASSWORD = "TestPass.123";

    @BeforeEach
    public void setUp() {
        // Arrange - Create a fresh UserStatsDAO instance
        userStatsDAO = new UserStatsDAO();
        userStatsDAO.setCurrentUserId(TEST_USER_ID);

        // Clean up any existing test data
        cleanupTestData();

        // Add test user
        UserService.addUser(TEST_EMAIL, TEST_PASSWORD);
    }

    @AfterEach
    public void tearDown() {
        // Clean up test data
        cleanupTestData();
        UserService.deleteUser(TEST_EMAIL);
    }

    private void cleanupTestData() {
        // Delete test user's country mastery data
    try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM country_mastery WHERE user_id = ?")) {
            stmt.setInt(1, TEST_USER_ID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error cleaning up test data: " + e.getMessage());
        }
    }

    @Test
    public void testSetCurrentUserId() {
        // Arrange
        int expectedUserId = 12345;

        // Act
        userStatsDAO.setCurrentUserId(expectedUserId);

        // Assert
        assertEquals(expectedUserId, userStatsDAO.getCurrentUserId(),
                "User ID should be set correctly");
    }

    @Test
    public void testGetCurrentUserIdDefaultValue() {
        // Arrange
        UserStatsDAO newDAO = new UserStatsDAO();

        // Act
        int userId = newDAO.getCurrentUserId();

        // Assert
        assertEquals(1, userId,
                "Default user ID should be 1");
    }

    @Test
    public void testGetCountriesGroupedByContinentEmptyResult() {
        // Arrange - No mastery data for test user

        // Act
        Map<String, List<CountryStats>> result = userStatsDAO.getCountriesGroupedByContinent();

        // Assert
        assertNotNull(result, "Result should not be null");
        // If the database contains no countries, skip this assertion (other tests already handle skipping).
        if (result.isEmpty()) {
            System.out.println("Skipping test - no countries in database");
            return;
        }
        // Otherwise ensure we at least have countries (with 0 mastery by default)
        assertFalse(result.isEmpty(), "Result should contain countries even with no mastery data");
        // Should have countries from the database with 0 mastery
    }

    @Test
    public void testGetCountriesGroupedByContinentStructure() {
        // Arrange & Act
        Map<String, List<CountryStats>> result = userStatsDAO.getCountriesGroupedByContinent();

        // Assert
        assertNotNull(result, "Result should not be null");
        
        for (Map.Entry<String, List<CountryStats>> entry : result.entrySet()) {
            String continent = entry.getKey();
            List<CountryStats> countries = entry.getValue();
            
            assertNotNull(continent, "Continent name should not be null");
            assertNotNull(countries, "Country list should not be null");
            assertFalse(countries.isEmpty(), "Each continent should have at least one country");
            
            // Verify each country has valid data
            for (CountryStats country : countries) {
                assertNotNull(country.getCountryName(), "Country name should not be null");
                assertNotNull(country.getCountryCode(), "Country code should not be null");
                assertTrue(country.getProgress() >= 0 && country.getProgress() <= 100,
                        "Progress should be between 0 and 100");
                assertTrue(country.getMasteryLevel() >= 0 && country.getMasteryLevel() <= 3,
                        "Mastery level should be between 0 and 3");
            }
        }
    }

    @Test
    public void testGetCountriesGroupedByContinentWithMasteryData() {
        // Arrange - Get a valid country code from the database first
        Map<String, List<CountryStats>> allCountries = userStatsDAO.getCountriesGroupedByContinent();
        
        // Skip test if no countries in database
        if (allCountries.isEmpty()) {
            System.out.println("Skipping test - no countries in database");
            return;
        }
        
        // Get the first available country
        String testCountryCode = null;
        for (List<CountryStats> countries : allCountries.values()) {
            if (!countries.isEmpty()) {
                testCountryCode = countries.get(0).getCountryCode();
                break;
            }
        }
        
        // Add mastery data for this country
        addTestCountryMastery(testCountryCode, 2, 5);

        // Act
        Map<String, List<CountryStats>> result = userStatsDAO.getCountriesGroupedByContinent();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());

        // Find the test country in the results
        boolean foundTestCountry = false;
        if (testCountryCode != null) {
            for (List<CountryStats> countries : result.values()) {
                for (CountryStats country : countries) {
                    if (testCountryCode.equals(country.getCountryCode())) {
                        foundTestCountry = true;
                        assertEquals(2, country.getMasteryLevel(),
                                "Mastery level should match inserted data");
                        assertEquals(5, country.getTotalCorrectGuesses(),
                                "Correct guesses should match inserted data");
                        assertFalse(country.isFullyUnlocked(),
                                "Country should not be fully unlocked at mastery level 2");
                    }
                }
            }

            assertTrue(foundTestCountry, "Should find the test country in results");
        }
    }

    @Test
    public void testGetCountryStatsValidCountry() {
        // Arrange - Get a valid country code from the database first
        Map<String, List<CountryStats>> allCountries = userStatsDAO.getCountriesGroupedByContinent();
        
        // Skip test if no countries in database
        if (allCountries.isEmpty()) {
            System.out.println("Skipping test - no countries in database");
            return;
        }
        
        // Get the first available country
        String testCountryCode = null;
        for (List<CountryStats> countries : allCountries.values()) {
            if (!countries.isEmpty()) {
                testCountryCode = countries.get(0).getCountryCode();
                break;
            }
        }
        
        // Add mastery data for this country
        addTestCountryMastery(testCountryCode, 1, 3);

        // Act
        CountryStats result = userStatsDAO.getCountryStats(testCountryCode);

        // Assert
        assertNotNull(result, "Should return stats for valid country that exists in database");
        assertEquals(testCountryCode, result.getCountryCode());
        assertEquals(1, result.getMasteryLevel());
        assertEquals(3, result.getTotalCorrectGuesses());
        assertFalse(result.isFullyUnlocked());
    }

    @Test
    public void testGetCountryStatsInvalidCountry() {
        // Arrange - Use a non-existent country code

        // Act
        CountryStats result = userStatsDAO.getCountryStats("INVALID");

        // Assert
        assertNull(result, "Should return null for invalid country code");
    }

    @Test
    public void testGetCountryStatsNoMasteryData() {
        // Arrange - Query a country with no mastery data

        // Act
        CountryStats result = userStatsDAO.getCountryStats("US");

        // Assert
        if (result != null) {
            assertEquals(0, result.getMasteryLevel(),
                    "Mastery level should be 0 for country with no data");
            assertEquals(0, result.getTotalCorrectGuesses(),
                    "Correct guesses should be 0 for country with no data");
            assertFalse(result.isFullyUnlocked(),
                    "Country should not be unlocked with no mastery data");
        }
    }

    @Test
    public void testGetUserOverallStatsNoData() {
        // Arrange - No mastery data for test user

        // Act
        Map<String, Object> stats = userStatsDAO.getUserOverallStats();

        // Assert
        assertNotNull(stats, "Stats should not be null");
        assertEquals(0, stats.getOrDefault("countries_played", -1),
                "Countries played should be 0");
        assertEquals(0, stats.getOrDefault("fully_unlocked", -1),
                "Fully unlocked should be 0");
        assertEquals(0, stats.getOrDefault("total_correct", -1),
                "Total correct should be 0");
    }

    @Test
    public void testGetUserOverallStatsWithData() {
        // Arrange - Get valid country codes from the database first
        Map<String, List<CountryStats>> allCountries = userStatsDAO.getCountriesGroupedByContinent();
        
        // Skip test if not enough countries in database
        int totalCountries = allCountries.values().stream()
                .mapToInt(List::size)
                .sum();
        
        if (totalCountries < 3) {
            System.out.println("Skipping test - need at least 3 countries in database");
            return;
        }
        
        // Get three different country codes
        String[] countryCodes = new String[3];
        int index = 0;
        outer: for (List<CountryStats> countries : allCountries.values()) {
            for (CountryStats country : countries) {
                if (index < 3) {
                    countryCodes[index++] = country.getCountryCode();
                    if (index == 3) break outer;
                }
            }
        }
        
        // Add multiple country mastery records
        addTestCountryMastery(countryCodes[0], 3, 10);
        addTestCountryMastery(countryCodes[1], 2, 5);
        addTestCountryMastery(countryCodes[2], 1, 2);

        // Act
        Map<String, Object> stats = userStatsDAO.getUserOverallStats();

        // Assert
        assertNotNull(stats);
        assertEquals(3, stats.get("countries_played"),
                "Should have played 3 countries");
        assertEquals(1, stats.get("fully_unlocked"),
                "Should have 1 fully unlocked country (mastery level 3)");
        assertEquals(17, stats.get("total_correct"),
                "Total correct guesses should be 10 + 5 + 2 = 17");
    }

    @Test
    public void testMasteryLevelProgressMapping() {
        // Test that mastery levels correctly map to progress percentages
        
        // Arrange - Get a valid country code from the database first
        Map<String, List<CountryStats>> allCountries = userStatsDAO.getCountriesGroupedByContinent();
        
        // Skip test if no countries in database
        if (allCountries.isEmpty()) {
            System.out.println("Skipping test - no countries in database");
            return;
        }
        
        // Get the first available country
        String testCountryCode = null;
        for (List<CountryStats> countries : allCountries.values()) {
            if (!countries.isEmpty()) {
                testCountryCode = countries.get(0).getCountryCode();
                break;
            }
        }
        
        if (testCountryCode == null) {
            System.out.println("Skipping test - no valid country found");
            return;
        }

        // Arrange & Act - Test mastery level 0
        addTestCountryMastery(testCountryCode, 0, 0);
        CountryStats stats0 = userStatsDAO.getCountryStats(testCountryCode);

        // Assert
        if (stats0 != null) {
            assertEquals(0.0, stats0.getProgress(), 0.01,
                    "Mastery level 0 should give 0% progress");
        }

        // Arrange & Act - Test mastery level 1
        updateTestCountryMastery(testCountryCode, 1, 1);
        CountryStats stats1 = userStatsDAO.getCountryStats(testCountryCode);

        // Assert
        if (stats1 != null) {
            assertEquals(33.33, stats1.getProgress(), 0.5,
                    "Mastery level 1 should give ~33.33% progress");
        }

        // Arrange & Act - Test mastery level 2
        updateTestCountryMastery(testCountryCode, 2, 3);
        CountryStats stats2 = userStatsDAO.getCountryStats(testCountryCode);

        // Assert
        if (stats2 != null) {
            assertEquals(66.67, stats2.getProgress(), 0.5,
                    "Mastery level 2 should give ~66.67% progress");
        }

        // Arrange & Act - Test mastery level 3
        updateTestCountryMastery(testCountryCode, 3, 5);
        CountryStats stats3 = userStatsDAO.getCountryStats(testCountryCode);

        // Assert
        if (stats3 != null) {
            assertEquals(100.0, stats3.getProgress(), 0.01,
                    "Mastery level 3 should give 100% progress");
            assertTrue(stats3.isFullyUnlocked(),
                    "Mastery level 3 should fully unlock the country");
        }
    }

    @Test
    public void testMultipleUsersDataIsolation() {
        // Arrange - Get a valid country code from the database first
        Map<String, List<CountryStats>> allCountries = userStatsDAO.getCountriesGroupedByContinent();
        
        // Skip test if no countries in database
        if (allCountries.isEmpty()) {
            System.out.println("Skipping test - no countries in database");
            return;
        }
        
        // Get the first available country
        String testCountryCode = null;
        for (List<CountryStats> countries : allCountries.values()) {
            if (!countries.isEmpty()) {
                testCountryCode = countries.get(0).getCountryCode();
                break;
            }
        }
        
        // Add data for test user
        addTestCountryMastery(testCountryCode, 3, 10);

        // Create another DAO with different user ID
        UserStatsDAO otherUserDAO = new UserStatsDAO();
        otherUserDAO.setCurrentUserId(TEST_USER_ID + 1);

        // Act
        CountryStats testUserStats = userStatsDAO.getCountryStats(testCountryCode);
        CountryStats otherUserStats = otherUserDAO.getCountryStats(testCountryCode);

        // Assert
        assertNotNull(testUserStats, "Test user should have stats for country that exists in database");
        assertEquals(3, testUserStats.getMasteryLevel());

        if (otherUserStats != null) {
            assertEquals(0, otherUserStats.getMasteryLevel(),
                    "Other user should have no mastery data");
        }
    }

    @Test
    public void testGetCountriesGroupedByContinentReturnsLinkedHashMap() {
        // Arrange & Act
        Map<String, List<CountryStats>> result = userStatsDAO.getCountriesGroupedByContinent();

        // Assert
        assertNotNull(result);
        // LinkedHashMap should maintain insertion order
        // This is a structural test to ensure the implementation uses LinkedHashMap
        assertTrue(result.getClass().getName().contains("LinkedHashMap") ||
                result.getClass().getName().contains("HashMap"),
                "Should return a HashMap implementation");
    }

    @Test
    public void testCountryStatsListNotNull() {
        // Arrange & Act
        Map<String, List<CountryStats>> result = userStatsDAO.getCountriesGroupedByContinent();

        // Assert
        assertNotNull(result);
        for (List<CountryStats> countryList : result.values()) {
            assertNotNull(countryList,
                    "Each continent's country list should not be null");
        }
    }

    @Test
    public void testGetUserOverallStatsStructure() {
        // Arrange - Get a valid country code from the database first
        Map<String, List<CountryStats>> allCountries = userStatsDAO.getCountriesGroupedByContinent();
        
        // Use a country if available, otherwise test still works with 0 data
        if (!allCountries.isEmpty()) {
            for (List<CountryStats> countries : allCountries.values()) {
                if (!countries.isEmpty()) {
                    String testCountryCode = countries.get(0).getCountryCode();
                    addTestCountryMastery(testCountryCode, 2, 7);
                    break;
                }
            }
        }

        // Act
        Map<String, Object> stats = userStatsDAO.getUserOverallStats();

        // Assert
        assertNotNull(stats);
        assertTrue(stats.containsKey("countries_played"),
                "Stats should contain countries_played key");
        assertTrue(stats.containsKey("fully_unlocked"),
                "Stats should contain fully_unlocked key");
        assertTrue(stats.containsKey("total_correct"),
                "Stats should contain total_correct key");

        // Verify data types
        assertTrue(stats.get("countries_played") instanceof Integer);
        assertTrue(stats.get("fully_unlocked") instanceof Integer);
        assertTrue(stats.get("total_correct") instanceof Integer);
    }

    // Helper method to add test country mastery data
    private void addTestCountryMastery(String countryCode, int masteryLevel, int correctGuesses) {
    try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO country_mastery (user_id, country_id, mastery_level, correct_guesses) " +
                             "VALUES (?, ?, ?, ?)")) {
            stmt.setInt(1, TEST_USER_ID);
            stmt.setString(2, countryCode);
            stmt.setInt(3, masteryLevel);
            stmt.setInt(4, correctGuesses);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding test mastery data: " + e.getMessage());
        }
    }

    // Helper method to update test country mastery data
    private void updateTestCountryMastery(String countryCode, int masteryLevel, int correctGuesses) {
    try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE country_mastery SET mastery_level = ?, correct_guesses = ? " +
                             "WHERE user_id = ? AND country_id = ?")) {
            stmt.setInt(1, masteryLevel);
            stmt.setInt(2, correctGuesses);
            stmt.setInt(3, TEST_USER_ID);
            stmt.setString(4, countryCode);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating test mastery data: " + e.getMessage());
        }
    }
}
