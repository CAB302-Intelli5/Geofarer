package controllers;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

public class CountryDetailControllerTest {

    private CountryDetailController controller;

    @BeforeAll
    static void initJFX() {
        // Initialize JavaFX toolkit for testing
        Platform.startup(() -> {});
    }

    @BeforeEach
    void setUp() {
        controller = new CountryDetailController();
    }

    @Test
    void testShowHints() {
        // Test showing hints with some unlocked
        List<String> unlockedHints = Arrays.asList("First hint", "Second hint");

        // This would normally require JavaFX components to be initialized
        // For now, just test that the method doesn't throw exceptions
        assertDoesNotThrow(() -> controller.showHints(unlockedHints, 4));
    }
}