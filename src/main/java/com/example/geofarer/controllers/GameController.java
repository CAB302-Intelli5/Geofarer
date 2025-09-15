package com.example.geofarer.controllers;

import com.example.geofarer.services.MapService;
import com.example.geofarer.utils.SceneManager;
import com.example.geofarer.views.LandingPageView;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.List;

public class GameController {

    public void goBackToLanding() {
        SceneManager.switchToScene(new LandingPageView());
    }


    public void handleMapClick(MouseEvent event, double displayedW, double displayedH, 
                              List<MapService.FeatureInfo> featureInfos, Label countryLabel) {
        
        if (displayedW <= 0 || displayedH <= 0) {
            System.err.println("Overlay dimensions not ready for click handling.");
            return;
        }

                                
        //The inner pane is designed to be seperate and therefore we get the width and height
        //Get the Coords from click
        double clickX = event.getX();
        double clickY = event.getY();

            // Transform the coordinates back to the original coordinate space
    javafx.scene.transform.Transform transform = ((Pane) event.getSource()).getTransforms().isEmpty()
            ? null
            : ((Pane) event.getSource()).getTransforms().get(0);

    if (transform != null) {
        try {
            javafx.scene.transform.Transform inverseTransform = transform.createInverse();
            javafx.geometry.Point2D originalPoint = inverseTransform.transform(clickX, clickY);
            clickX = originalPoint.getX();
            clickY = originalPoint.getY();
        } catch (javafx.scene.transform.NonInvertibleTransformException e) {
            System.err.println("Could not invert transform for click handling: " + e.getMessage());
            return;
        }
    }

        //Now I need to convert these UI coords to the WGS84 which natrual earth uses
        double lon = (clickX / displayedW) * 360 - 180.0; //Formula to convert to longitude
        double lat = 90.0 - (clickY / displayedH) * 180.0; //Formula for lat

        //Using the Java Topology Suite (JTS) fund the country
        GeometryFactory geomFactory = JTSFactoryFinder.getGeometryFactory();
        Point clickedPoint = geomFactory.createPoint(new Coordinate(lon, lat));

        //Now we have the point lets finds the country that contains the clicked.

        String countryName = "Unknown";
        for (MapService.FeatureInfo fi: featureInfos) {
            if (fi.geom.contains(clickedPoint)){
                countryName = fi.name;
                System.out.println(countryName);
                break; // Found the country, stop searching
            }
        }

        //Update the country label
        countryLabel.setText("Clicked: " + countryName);
    }
}
