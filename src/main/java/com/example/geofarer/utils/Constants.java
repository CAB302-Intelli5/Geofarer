package com.example.geofarer.utils;

public class Constants {
    // Map data paths
    public static final String RASTER_PATH = "src/main/resources/naturalearth/NE1_50M_SR_W/NE1_50M_SR_W.tif";
    public static final String SHAPEFILE_PATH = "src/main/resources/naturalearth/ne_50m_admin_0_countries/ne_50m_admin_0_countries.shp";

    // UI Constants
    public static final double MAP_AREA_FACTOR = 0.90;
    public static final int DEFAULT_WINDOW_WIDTH = 1200;
    public static final int DEFAULT_WINDOW_HEIGHT = 800;

    // Map sizing constants
    public static final double MIN_MAP_WIDTH = 400;
    public static final double MAX_MAP_WIDTH = 4000;
    public static final double MIN_MAP_HEIGHT = 300;
    public static final double MAX_MAP_HEIGHT = 3000;

    // Game Constants
    public static final int POINTS_PER_CORRECT_ANSWER = 100;
    public static final int TIME_LIMIT_SECONDS = 30;

    // Performance constants
    public static final boolean USE_BACKGROUND_LOADING = true;
    public static final double MAP_STROKE_WIDTH_FACTOR = 0.4;
    public static final double MIN_STROKE_WIDTH = 0.2;
}
