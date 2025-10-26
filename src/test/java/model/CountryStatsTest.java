package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Headless tests for CountryStats model following AAA pattern
 */
public class CountryStatsTest {

    private CountryStats countryStats;

    @BeforeEach
    public void setUp() {
        // Arrange - Create a fresh CountryStats instance before each test
        countryStats = new CountryStats("Australia", "AU", 50.0, false, 1);
    }

    @Test
    public void testConstructorWithAllParameters() {
        // Arrange & Act
        CountryStats stats = new CountryStats("France", "FR", 100.0, true, 3);

        // Assert
        assertEquals("France", stats.getCountryName());
        assertEquals("FR", stats.getCountryCode());
        assertEquals(100.0, stats.getProgress(), 0.01);
        assertTrue(stats.isFullyUnlocked());
        assertEquals(3, stats.getMasteryLevel());
        assertEquals(0, stats.getTotalCorrectGuesses());
    }

    @Test
    public void testConstructorWithMinimalParameters() {
        // Arrange & Act
        CountryStats stats = new CountryStats("Japan", "JP");

        // Assert
        assertEquals("Japan", stats.getCountryName());
        assertEquals("JP", stats.getCountryCode());
        assertEquals(0.0, stats.getProgress(), 0.01);
        assertFalse(stats.isFullyUnlocked());
        assertEquals(0, stats.getMasteryLevel());
    }

    @Test
    public void testSetProgressWithinBounds() {
        // Arrange
        double expectedProgress = 75.5;

        // Act
        countryStats.setProgress(expectedProgress);

        // Assert
        assertEquals(expectedProgress, countryStats.getProgress(), 0.01);
    }

    @Test
    public void testSetProgressAboveMaximum() {
        // Arrange
        double excessiveProgress = 150.0;

        // Act
        countryStats.setProgress(excessiveProgress);

        // Assert
        assertEquals(100.0, countryStats.getProgress(), 0.01, 
                "Progress should be capped at 100");
    }

    @Test
    public void testSetProgressBelowMinimum() {
        // Arrange
        double negativeProgress = -25.0;

        // Act
        countryStats.setProgress(negativeProgress);

        // Assert
        assertEquals(0.0, countryStats.getProgress(), 0.01, 
                "Progress should not go below 0");
    }

    @Test
    public void testSetFullyUnlockedTrue() {
        // Arrange
        assertFalse(countryStats.isFullyUnlocked(), "Should start as locked");

        // Act
        countryStats.setFullyUnlocked(true);

        // Assert
        assertTrue(countryStats.isFullyUnlocked());
    }

    @Test
    public void testSetFullyUnlockedFalse() {
        // Arrange
        CountryStats unlockedStats = new CountryStats("USA", "US", 100.0, true, 3);

        // Act
        unlockedStats.setFullyUnlocked(false);

        // Assert
        assertFalse(unlockedStats.isFullyUnlocked());
    }

    @Test
    public void testSetMasteryLevelWithinBounds() {
        // Arrange
        int expectedMastery = 2;

        // Act
        countryStats.setMasteryLevel(expectedMastery);

        // Assert
        assertEquals(expectedMastery, countryStats.getMasteryLevel());
    }

    @Test
    public void testSetMasteryLevelAboveMaximum() {
        // Arrange
        int excessiveMastery = 5;

        // Act
        countryStats.setMasteryLevel(excessiveMastery);

        // Assert
        assertEquals(3, countryStats.getMasteryLevel(), 
                "Mastery level should be capped at 3");
    }

    @Test
    public void testSetMasteryLevelBelowMinimum() {
        // Arrange
        int negativeMastery = -2;

        // Act
        countryStats.setMasteryLevel(negativeMastery);

        // Assert
        assertEquals(0, countryStats.getMasteryLevel(), 
                "Mastery level should not go below 0");
    }

    @Test
    public void testSetMasteryLevelUpdatesProgress() {
        // Arrange
        int masteryLevel = 2;
        double expectedProgress = (2.0 * 100.0) / 3.0; // 66.67%

        // Act
        countryStats.setMasteryLevel(masteryLevel);

        // Assert
        assertEquals(expectedProgress, countryStats.getProgress(), 0.01, 
                "Progress should be calculated based on mastery level");
    }

    @Test
    public void testSetMasteryLevel3UnlocksCountry() {
        // Arrange
        assertFalse(countryStats.isFullyUnlocked(), "Should start as locked");

        // Act
        countryStats.setMasteryLevel(3);

        // Assert
        assertTrue(countryStats.isFullyUnlocked(), 
                "Mastery level 3 should fully unlock the country");
        assertEquals(100.0, countryStats.getProgress(), 0.01, 
                "Progress should be 100% at mastery level 3");
    }

    @Test
    public void testSetMasteryLevel0DoesNotUnlock() {
        // Arrange & Act
        countryStats.setMasteryLevel(0);

        // Assert
        assertFalse(countryStats.isFullyUnlocked(), 
                "Mastery level 0 should not unlock the country");
        assertEquals(0.0, countryStats.getProgress(), 0.01);
    }

    @Test
    public void testSetMasteryLevel1DoesNotUnlock() {
        // Arrange & Act
        countryStats.setMasteryLevel(1);

        // Assert
        assertFalse(countryStats.isFullyUnlocked(), 
                "Mastery level 1 should not fully unlock the country");
    }

    @Test
    public void testSetMasteryLevel2DoesNotUnlock() {
        // Arrange & Act
        countryStats.setMasteryLevel(2);

        // Assert
        assertFalse(countryStats.isFullyUnlocked(), 
                "Mastery level 2 should not fully unlock the country");
    }

    @Test
    public void testSetTotalCorrectGuesses() {
        // Arrange
        int expectedGuesses = 15;

        // Act
        countryStats.setTotalCorrectGuesses(expectedGuesses);

        // Assert
        assertEquals(expectedGuesses, countryStats.getTotalCorrectGuesses());
    }

    @Test
    public void testGetMasteryStarsLevel0() {
        // Arrange
        countryStats.setMasteryLevel(0);

        // Act
        String stars = countryStats.getMasteryStars();

        // Assert
        assertEquals("☆☆☆", stars, 
                "Level 0 should show three empty stars");
    }

    @Test
    public void testGetMasteryStarsLevel1() {
        // Arrange
        countryStats.setMasteryLevel(1);

        // Act
        String stars = countryStats.getMasteryStars();

        // Assert
        assertEquals("★☆☆", stars, 
                "Level 1 should show one filled star and two empty stars");
    }

    @Test
    public void testGetMasteryStarsLevel2() {
        // Arrange
        countryStats.setMasteryLevel(2);

        // Act
        String stars = countryStats.getMasteryStars();

        // Assert
        assertEquals("★★☆", stars, 
                "Level 2 should show two filled stars and one empty star");
    }

    @Test
    public void testGetMasteryStarsLevel3() {
        // Arrange
        countryStats.setMasteryLevel(3);

        // Act
        String stars = countryStats.getMasteryStars();

        // Assert
        assertEquals("★★★", stars, 
                "Level 3 should show three filled stars");
    }

    @Test
    public void testToStringFormat() {
        // Arrange
        countryStats.setMasteryLevel(2);

        // Act
        String result = countryStats.toString();

        // Assert
        assertTrue(result.contains("Australia"), "Should contain country name");
        assertTrue(result.contains("AU"), "Should contain country code");
        assertTrue(result.contains("Mastery 2/3"), "Should contain mastery level");
        assertTrue(result.contains("LOCKED"), "Should indicate locked status");
    }

    @Test
    public void testToStringFormatFullyUnlocked() {
        // Arrange
        countryStats.setMasteryLevel(3);

        // Act
        String result = countryStats.toString();

        // Assert
        assertTrue(result.contains("UNLOCKED"), "Should indicate unlocked status");
        assertTrue(result.contains("100.0%"), "Should show 100% progress");
    }

    @Test
    public void testProgressCalculationMasteryLevel0() {
        // Arrange & Act
        countryStats.setMasteryLevel(0);

        // Assert
        assertEquals(0.0, countryStats.getProgress(), 0.01);
    }

    @Test
    public void testProgressCalculationMasteryLevel1() {
        // Arrange & Act
        countryStats.setMasteryLevel(1);

        // Assert
        double expectedProgress = (1.0 * 100.0) / 3.0; // 33.33%
        assertEquals(expectedProgress, countryStats.getProgress(), 0.01);
    }

    @Test
    public void testProgressCalculationMasteryLevel2() {
        // Arrange & Act
        countryStats.setMasteryLevel(2);

        // Assert
        double expectedProgress = (2.0 * 100.0) / 3.0; // 66.67%
        assertEquals(expectedProgress, countryStats.getProgress(), 0.01);
    }

    @Test
    public void testProgressCalculationMasteryLevel3() {
        // Arrange & Act
        countryStats.setMasteryLevel(3);

        // Assert
        assertEquals(100.0, countryStats.getProgress(), 0.01);
    }

    @Test
    public void testMultipleSetMasteryLevelCalls() {
        // Arrange & Act
        countryStats.setMasteryLevel(1);
        assertEquals(1, countryStats.getMasteryLevel());
        
        countryStats.setMasteryLevel(2);
        assertEquals(2, countryStats.getMasteryLevel());
        
        countryStats.setMasteryLevel(3);

        // Assert
        assertEquals(3, countryStats.getMasteryLevel());
        assertEquals(100.0, countryStats.getProgress(), 0.01);
        assertTrue(countryStats.isFullyUnlocked());
    }

    @Test
    public void testMasteryLevelDecrement() {
        // Arrange
        countryStats.setMasteryLevel(3);
        assertTrue(countryStats.isFullyUnlocked());

        // Act
        countryStats.setMasteryLevel(1);

        // Assert
        assertEquals(1, countryStats.getMasteryLevel());
        assertFalse(countryStats.isFullyUnlocked(), 
                "Decreasing mastery level should lock the country");
    }

    @Test
    public void testCountryStatsImmutabilityOfNames() {
        // Arrange
        String originalName = countryStats.getCountryName();
        String originalCode = countryStats.getCountryCode();

        // Act - Try to modify other properties
        countryStats.setMasteryLevel(3);
        countryStats.setProgress(100.0);
        countryStats.setFullyUnlocked(true);

        // Assert - Name and code should remain unchanged
        assertEquals(originalName, countryStats.getCountryName());
        assertEquals(originalCode, countryStats.getCountryCode());
    }
}
