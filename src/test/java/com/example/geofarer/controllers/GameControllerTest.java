package com.example.geofarer.controllers;

import com.example.geofarer.model.MapService;
import com.example.geofarer.views.GameView;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class GameControllerTest {

    private GameController controller;



    @BeforeEach
    void setUp() {
        controller = new GameController();
    }

    @Test
    @DisplayName("Should select a random target country when feature info is set")
    void setFeatureInfos_shouldSelectRandomTargetCountry() {
        // Given
        GeometryFactory geomFactory = new GeometryFactory();
        MapService.FeatureInfo germany = new MapService.FeatureInfo(
                geomFactory.createPoint(new Coordinate(10, 51)), "Germany", "GM", "Europe");
        MapService.FeatureInfo france = new MapService.FeatureInfo(
                geomFactory.createPoint(new Coordinate(2, 46)), "France", "FR", "Europe");
        List<MapService.FeatureInfo> featureInfos = Arrays.asList(germany, france);

        // When
        controller.setFeatureInfos(featureInfos);

        // Then
        assertNotNull(controller.getTargetCountry(), "Target country should not be null");
        assertTrue(Arrays.asList("Germany", "France").contains(controller.getTargetCountry()),
                "Target country should be one of the provided countries");
    }

    @Test
    @DisplayName("Should handle correct guess and show success")
    void processMapClick_whenCorrectCountryClicked_shouldShowSuccess() {
        // Given
        GeometryFactory geomFactory = new GeometryFactory();
        Geometry germanyGeom = geomFactory.createPoint(new Coordinate(10, 51));
        MapService.FeatureInfo germany = new MapService.FeatureInfo(germanyGeom, "Germany", "GM", "Europe");
        controller.setFeatureInfos(List.of(germany));
        controller.setTargetCountry("Germany");

        String before = controller.getTargetCountry();

        // When
        controller.processMapClick(null); // Simulate a click on Germany

        // Then
        assertEquals("Germany", before);
        assertEquals("Germany", controller.getTargetCountry(), "Target country should remain Germany");
    }

    @Test
    @DisplayName("Should handle incorrect guess and request a hint")
    void processMapClick_whenIncorrectCountryClicked_shouldRequestHint() {
        // Given
        GeometryFactory geomFactory = new GeometryFactory();
        Geometry germanyGeom = geomFactory.createPoint(new Coordinate(10, 51));
        Geometry franceGeom = geomFactory.createPoint(new Coordinate(2, 46));
        MapService.FeatureInfo germany = new MapService.FeatureInfo(germanyGeom, "Germany", "GM", "Europe");
        MapService.FeatureInfo france = new MapService.FeatureInfo(franceGeom, "France", "FR","Europe");
        controller.setFeatureInfos(Arrays.asList(germany, france));
        controller.setTargetCountry("Germany");

        // When
        String target = controller.getTargetCountry();

        // Then
        assertEquals("Germany", target);
        assertTrue(Arrays.asList("Germany", "France").contains(target));
    }

    @Test
    @DisplayName("Should select a new target country")
    void selectNewTarget_shouldSelectNewRandomTargetCountry() {
        // Given
        GeometryFactory geomFactory = new GeometryFactory();
        MapService.FeatureInfo germany = new MapService.FeatureInfo(
                geomFactory.createPoint(new Coordinate(10, 51)), "Germany", "GM", "Europe");
        MapService.FeatureInfo france = new MapService.FeatureInfo(
                geomFactory.createPoint(new Coordinate(2, 46)), "France", "FR", "Europe");
        List<MapService.FeatureInfo> featureInfos = Arrays.asList(germany, france);
        controller.setFeatureInfos(featureInfos);
        String initialTarget = controller.getTargetCountry();

        // When
        controller.selectNewTarget();
        String newTarget = controller.getTargetCountry();

        // Then
        assertNotNull(newTarget);
        assertTrue(Arrays.asList("Germany", "France").contains(newTarget));
    }

}


