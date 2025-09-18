package com.example.geofarer.controllers;

import com.example.geofarer.services.MapService;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GameControllerTest {

    @BeforeAll
    static void initToolkit() {
        // Ensure headless mode for JavaFX
        System.setProperty("java.awt.headless", "true");
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        Platform.startup(() -> {});
    }

    private GameController controller;
    private Label targetCountryLabel;
    private Label countryLabel;
    private Pane overlay;
    private StackPane innerMapPane;
    private StackPane mapContainer;
    private String region;
    private String gec;
    private TextArea handleHintArea;
    private Button viewSuccessButton;


    @BeforeEach
    void setUp() {
        controller = new GameController();
        targetCountryLabel = mock(Label.class);
        countryLabel = mock(Label.class);
        overlay = mock(Pane.class);
        innerMapPane = mock(StackPane.class);
        mapContainer = mock(StackPane.class);

        controller.initializeController(targetCountryLabel, countryLabel, handleHintArea,  overlay, innerMapPane, mapContainer,viewSuccessButton);
    }

    @Test
    void testSetFeatureInfosSelectsTargetCountry() {
        String continent = "Europe";
        String fips10 = "gm"; //germany
        GeometryFactory geomFactory = new GeometryFactory();
        MapService.FeatureInfo country = new MapService.FeatureInfo(geomFactory.createPoint(new Coordinate(0,0)), "TestLand", continent, fips10);

        controller.setFeatureInfos(Collections.singletonList(country));

        // Target country should be set to "TestLand"
        assertEquals("TestLand", controller.getTargetCountry());
        verify(targetCountryLabel).setText("Target Country: TestLand");
    }

    @Test
    void testProcessMapClickUpdatesLabel() {
        GeometryFactory geomFactory = new GeometryFactory();
        String continent = "Europe";
        String fips10 = "gm"; //germany

        MapService.FeatureInfo country = new MapService.FeatureInfo(geomFactory.createPoint(new Coordinate(0,0)), "TestLand", continent, fips10);

        controller.setFeatureInfos(Collections.singletonList(country));
        controller.setTargetCountry("TestLand");

        // Mock overlay size
        when(overlay.getWidth()).thenReturn(100.0);
        when(overlay.getHeight()).thenReturn(100.0);

        // Mock innerMapPane.sceneToLocal to return the center
        when(innerMapPane.sceneToLocal(anyDouble(), anyDouble()))
                .thenReturn(new javafx.geometry.Point2D(50, 50));

        // Simulate click
        javafx.scene.input.MouseEvent mockEvent = mock(javafx.scene.input.MouseEvent.class);
        controller.processMapClick(mockEvent);

        verify(countryLabel).setText("Success! You clicked TestLand");
    }
}


